package maksim.booksservice.services;

import maksim.booksservice.exceptions.BadRequestException;
import maksim.booksservice.exceptions.NotFoundException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import maksim.booksservice.config.AppConfig;
import maksim.booksservice.exceptions.ConflictException;
import maksim.booksservice.models.dtos.BookDto;
import maksim.booksservice.models.dtos.UpdateBookDto;
import maksim.booksservice.models.entities.Book;
import maksim.booksservice.models.dtos.CreateBookDto;
import maksim.booksservice.models.entities.BookStatusLog;
import maksim.booksservice.models.entities.User;
import maksim.booksservice.models.kafkadtos.ChangeBookOneRateDto;
import maksim.booksservice.repositories.BookRepository;
import maksim.booksservice.repositories.UserRepository;
import maksim.booksservice.utils.bookutils.BookSearchCriteria;
import maksim.booksservice.utils.bookutils.BookSpecification;
import maksim.booksservice.utils.enums.BookStatus;
import maksim.booksservice.utils.enums.JoinMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BookService {
    private static final Logger logger = LoggerFactory.getLogger(BookService.class);

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final AppConfig appConfig;
    private final CachingService cachingService;

    @Autowired
    public BookService(
            BookRepository bookRepository,
            UserRepository userRepository,
            AppConfig appConfig,
            CachingService cachingService
    ) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.appConfig = appConfig;
        this.cachingService = cachingService;
    }

    private void saveOrThrow(Book book) {
        try {
            bookRepository.save(book);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Your review contains conflicted data");
        }
    }


    public BookDto getById(int id, JoinMode joinMode) {
        logger.trace("BookService method entrance: getById | Params: id {} ; join mode {}", id, joinMode);

        Optional<Book> book = switch (joinMode) {
            case WITH -> bookRepository.findByIdWithJoin(id);
            case WITHOUT -> bookRepository.findByIdWithoutJoin(id);
        };

        if (book.isEmpty()) {
            logger.trace("BookService method exception: getById");

            throw new NotFoundException("Cannot find book");
        }

        List<Map<String, String>> statuses = null;

        if (joinMode == JoinMode.WITH) {
            final String statusName =  "status_name";
            final String count = "count";

            statuses = Arrays.asList(
                new HashMap<>(Map.of(statusName, BookStatus.READ.toString(), count, "0")),
                new HashMap<>(Map.of(statusName, BookStatus.READING.toString(), count, "0")),
                new HashMap<>(Map.of(statusName, BookStatus.DROP.toString(), count, "0"))
            );

            int readCounter = 0;
            int readingCounter = 0;
            int dropCounter = 0;

            for (BookStatusLog log : book.get().getStatusesLogs()) {
                switch (BookStatus.fromValue(log.getStatus())) {
                    case READ -> readCounter++;
                    case READING -> readingCounter++;
                    case DROP -> dropCounter++;
                }
            }

            statuses.get(0).put(count, String.valueOf(readCounter));
            statuses.get(1).put(count, String.valueOf(readingCounter));
            statuses.get(2).put(count, String.valueOf(dropCounter));
        }

        logger.trace("BookService return: getAllBooks | Result is found");

        return new BookDto(book.get(), joinMode, statuses);
    }

    public List<BookDto> getAllBooks(BookSearchCriteria criteria, Pageable pageable) {
        logger.trace("BookService method entrance: getAllBooks | Params: {} ; {}", criteria, pageable);

        BookSpecification spec = new BookSpecification(criteria);

        List<Book> booksEntities = bookRepository.findAll(spec, pageable).toList();

        final List<BookDto> books = new ArrayList<>(booksEntities.size());

        if (criteria.getJoinModeForStatuses() == JoinMode.WITH) {
            final String statusName =  "status_name";
            final String minDate = "min_date";
            final String maxDate = "max_date";
            final String count = "count";

            final List<Map<String, String>> statuses = Arrays.asList(
                new HashMap<>(Map.of(
                    statusName, BookStatus.READ.toString(),
                    minDate, criteria.getStatusMinDate().toString(),
                    maxDate, criteria.getStatusMaxDate().toString(),
                    count, ""
                )),
                new HashMap<>(Map.of(
                    statusName, BookStatus.READING.toString(),
                    minDate, criteria.getStatusMinDate().toString(),
                    maxDate, criteria.getStatusMaxDate().toString(),
                    count, ""
                )),
                new HashMap<>(Map.of(
                    statusName, BookStatus.DROP.toString(),
                    minDate, criteria.getStatusMinDate().toString(),
                    maxDate, criteria.getStatusMaxDate().toString(),
                    count, ""
                ))
            );

            booksEntities.forEach(book -> {
                int readCounter = 0;
                int readingCounter = 0;
                int dropCounter = 0;

                for (BookStatusLog log : book.getStatusesLogs()) {
                    switch (BookStatus.fromValue(log.getStatus())) {
                        case READ -> readCounter++;
                        case READING -> readingCounter++;
                        case DROP -> dropCounter++;
                    }
                }

                statuses.get(0).put(count, String.valueOf(readCounter));
                statuses.get(1).put(count, String.valueOf(readingCounter));
                statuses.get(2).put(count, String.valueOf(dropCounter));

                books.add(new BookDto(book, criteria.getJoinModeForAuthor(), new ArrayList<>(statuses)));
            });
        } else {
            booksEntities.forEach(book ->
                books.add(new BookDto(book, criteria.getJoinModeForAuthor(), null))
            );
        }

        logger.trace("BookService return: getAllBooks | Result: found items {}", books.size());

        return books;
    }

    public File getFile(int bookId) {
        Optional<Book> book = bookRepository.findById(bookId);

        if (book.isEmpty() || book.get().getFilePath() == null) {
            throw new NotFoundException("Cannot find book");
        }

        File file = new File(appConfig.getBookFilesDirectory() + book.get().getFilePath());

        if (!file.exists()) {
            throw new NotFoundException("Cannot open book file" + file.getPath());
        }

        return file;
    }



    public BookDto addBookMetaData(CreateBookDto bookData) {
        logger.trace("BookService method entrance: addBookMetaData");

        Optional<User> author = userRepository.findById(bookData.getAuthorId());
        if (author.isEmpty()) {
            throw new BadRequestException("Cannot add book, cause cannot find user/author with such id");
        }

        Book book = new Book();

        book.setAuthor(author.get());
        book.setName(bookData.getName());
        book.setIssuedDate(new Date());
        book.setGenres(bookData.getGenres());

        saveOrThrow(book);

        logger.trace("BookService method end: addBookMetaData | Book metadata was created successfully");

        return new BookDto(book, null, null);
    }

    public void addBookFile(MultipartFile file, int bookId) {
        logger.trace("BookService method entrance: addBookFile");

        Path targetPath = new File(appConfig.getBookFilesDirectory()).toPath().normalize();
        File testFile = new File(appConfig.getBookFilesDirectory() + file.getOriginalFilename());

        if (!testFile.toPath().normalize().startsWith(targetPath)) {
            throw new BadRequestException("Invalid file name");
        }

        String fileName = testFile.getPath();

        if (fileName.isEmpty() || fileName.lastIndexOf(".") == -1) {
            throw new BadRequestException("Invalid file extension");
        }

        Optional<Book> book = bookRepository.findById(bookId);

        if (book.isEmpty()) {
            throw new BadRequestException("Cannot find book with such id");
        }

        String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1);
        File targetFile = null;

        try {
            Path uploadPath = Paths.get(appConfig.getBookFilesDirectory());
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            targetFile = new File(appConfig.getBookFilesDirectory() + bookId + "." + fileExtension);
            try (InputStream inputStream = file.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(targetFile)) {

                int read;
                byte[] bytes = new byte[1024];

                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }

                book.get().setFilePath(bookId + "." + fileExtension);

                saveOrThrow(book.get());
            }
        } catch (IOException e) {
            if (targetFile != null && targetFile.exists()) {
                try {
                    Files.delete(targetFile.toPath());
                } catch (Exception ex) {
                    logger.warn("Cannot delete file");
                }
            }

            throw new BadRequestException("Cannot open file");
        }

        logger.trace("BookService method return: addBookFile | File was added successfully");
    }



    public void deleteBook(int bookId) {
        logger.trace("BookService method entrance: deleteBook");

        Optional<Book> book = bookRepository.findById(bookId);

        if (book.isEmpty()) {
            throw new NotFoundException("Cannot access book with such id");
        }

        File file = new File(appConfig.getBookFilesDirectory() + book.get().getFilePath());
        if (file.exists()) {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (Exception e) {
                logger.warn("Cannot delete file {}", file.getPath());
            }
        }

        cachingService.deleteBook(book.get().getId());

        bookRepository.delete(book.get());

        logger.trace("BookService method return: deleteBook");
    }


    public BookDto updateBook(int bookId, UpdateBookDto bookDto) {
        logger.trace("BookService method entrance: updateBook");

        Optional<Book> book = bookRepository.findById(bookId);

        if (book.isEmpty()) {
            throw new NotFoundException("Cannot access book with such id");
        }

        boolean isSmthChanged = false;

        if (bookDto.getName() != null) {
            book.get().setName(bookDto.getName());

            isSmthChanged = true;
        }

        if (bookDto.getGenres() != null) {
            book.get().setGenres(bookDto.getGenres());

            isSmthChanged = true;
        }

        if (isSmthChanged) {
            saveOrThrow(book.get());
            cachingService.updateBook(bookId, new BookDto(book.get(), null, null));
        }

        logger.trace("BookService method return: updateBook");

        return new BookDto(book.get(), null, null);
    }



    public void changeOneRate(ChangeBookOneRateDto reviewData) {
        logger.trace("BookService method entrance: changeOneRate | Params: {}", reviewData);

        if (reviewData == null || reviewData.getBookId() == null
                || reviewData.getAction() == 0 || reviewData.getRating() == null) {
            logger.info("BookService method: changeOneRate | Values cannot be null (except previous rate), review data: {}", reviewData);

            return;
        }

        Optional<Book> optionalBook = bookRepository.findById(reviewData.getBookId());

        if (optionalBook.isEmpty()) {
            logger.info("BookService method: changeOneRate | Cannot find book with such id: {}", reviewData);

            return;
        }

        Book book = optionalBook.get();

        switch (reviewData.getAction()) {
            case -1 -> {
                if (book.getRatingCount() == 0) {
                    logger.info("BookService method: changeOneRate | Rating count is 0, review data: {}", reviewData);

                    return;
                }

                book.setRating(
                        (book.getRating() * book.getRatingCount() - reviewData.getRating()) / (book.getRatingCount() - 1)
                );

                book.setRatingCount(
                        book.getRatingCount() - 1
                );
            }
            case 0 -> {
                if (reviewData.getPreviousRate() == null) {
                    logger.info("BookService method: changeOneRate | Previous rate is null, review data: {}", reviewData);

                    return;
                }

                book.setRating(
                        (book.getRating() * book.getRatingCount() + reviewData.getPreviousRate()
                                - reviewData.getRating()) / book.getRatingCount()
                );
            }
            case 1 -> {
                book.setRating(
                        (book.getRating() * book.getRatingCount() + reviewData.getRating()) / (book.getRatingCount() + 1)
                );

                book.setRatingCount(
                        book.getRatingCount() + 1
                );
            }
            default -> {
                logger.info("BookService method: changeOneRate | Invalid action value, "
                        + "action can be -1 - remove / 0 - change / 1 - add rate, review data: {}", reviewData);

                return;
            }
        }

        try {
            bookRepository.save(book);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Your review contains conflicted data");
        }

        logger.trace("BookService method return: changeOneRate");
    }

}
