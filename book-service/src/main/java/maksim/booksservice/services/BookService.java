package maksim.booksservice.services;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import maksim.booksservice.config.AppConfig;
import maksim.booksservice.models.Book;
import maksim.booksservice.models.BookDtoForCreating;
import maksim.booksservice.models.User;
import maksim.booksservice.models.kafkadtos.DtoForBookReviewChanging;
import maksim.booksservice.repositories.BookRepository;
import maksim.booksservice.repositories.UserRepository;
import maksim.booksservice.utils.bookutils.BookSearchCriteria;
import maksim.booksservice.utils.bookutils.BookSpecification;
import maksim.booksservice.utils.enums.JoinMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BookService {
    private static final Logger logger = LoggerFactory.getLogger(BookService.class);

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final AppConfig appConfig;

    @Autowired
    public BookService(
            BookRepository bookRepository,
            UserRepository userRepository,
            AppConfig appConfig
    ) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.appConfig = appConfig;
    }

    public Book getById(int id, JoinMode joinMode) {
        logger.trace("BookService method entrance: getById | Params: id {} ; join mode {}", id, joinMode);

        Optional<Book> book = switch (joinMode) {
            case WITH_JOIN -> bookRepository.findByIdWithAuthor(id);
            case WITHOUT_JOIN -> bookRepository.findByIdWithoutAuthor(id);
        };

        if (book.isEmpty()) {
            throw new NotFoundException("Cannot find book");
        }

        if (joinMode == JoinMode.WITH_JOIN) {
            book.get().setBookAuthor(book.get().getNoneJsonAuthor());
        }

        logger.trace("BookService return: getAllBooks | Result is found");

        return book.get();
    }

    public List<Book> getAllBooks(BookSearchCriteria criteria, Pageable pageable) {
        logger.trace("BookService method entrance: getAllBooks | Params: {} ; {}", criteria, pageable);

        BookSpecification spec = new BookSpecification(criteria);

        List<Book> books = bookRepository.findAll(spec, pageable).toList();

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



    public Book addBookMetaData(BookDtoForCreating bookData) {
        logger.trace("BookService method entrance: addBookMetaData");

        Optional<User> author = userRepository.findById(bookData.getAuthorId());
        if (author.isEmpty()) {
            throw new BadRequestException("Cannot add book, cause cannot find user/author with such id");
        }

        Book book = new Book();

        book.setNoneJsonAuthor(author.get());
        book.setName(bookData.getName());
        book.setIssuedDate(new Date());
        book.setGenres(bookData.getGenres());

        bookRepository.save(book);

        logger.trace("BookService method end: addBookMetaData | Book metadata was created successfully");

        return book;
    }

    public void addBookFile(MultipartFile file, int bookId) {
        logger.trace("Try to add file for book");

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

                bookRepository.save(book.get());
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

        logger.trace("Book file was added successfully");
    }



    public void deleteBook(int bookId) {
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

        bookRepository.delete(book.get());
    }



    public void changeOneRate(DtoForBookReviewChanging reviewData) {
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

        bookRepository.save(book);

        logger.trace("BookService method return: changeOneRate");
    }

}
