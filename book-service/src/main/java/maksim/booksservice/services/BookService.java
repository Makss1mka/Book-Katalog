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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import maksim.booksservice.config.AppConfig;
import maksim.booksservice.models.Book;
import maksim.booksservice.models.BookDtoForCreating;
import maksim.booksservice.models.User;
import maksim.booksservice.models.kafkadtos.DtoForBookReviewChanging;
import maksim.booksservice.repositories.BookRepository;
import maksim.booksservice.repositories.BookStatusesRepository;
import maksim.booksservice.repositories.UserRepository;
import maksim.booksservice.utils.enums.BookStatusScope;
import maksim.booksservice.utils.enums.Operator;
import maksim.booksservice.utils.validators.FileValidators;
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
    private final BookStatusesRepository bookStatusesRepository;
    private final UserRepository userRepository;
    private final AppConfig appConfig;
    private final FileValidators fileValidators;

    private static final String ERROR_OPERATOR_MESSAGE = "Incorrect value for mode. Support next values: greater, less";

    @Autowired
    public BookService(
            BookRepository bookRepository,
            BookStatusesRepository bookStatusesRepository,
            UserRepository userRepository,
            AppConfig appConfig,
            FileValidators fileValidators
    ) {
        this.bookRepository = bookRepository;
        this.bookStatusesRepository = bookStatusesRepository;
        this.userRepository = userRepository;
        this.appConfig = appConfig;
        this.fileValidators = fileValidators;
    }

    public List<Book> getAllBooks(Pageable pageable) {
        logger.trace("Try to get books without filters");

        return bookRepository.findAll(pageable).toList();
    }

    public List<Book> getAllBooksWithFilters(String rawGenresFilter, Pageable pageable) {
        logger.trace("Try to get books with filters");

        List<String> genresFilter = Arrays.stream(rawGenresFilter.split(",")).toList();

        return bookRepository.findAllByGenres(genresFilter, pageable);
    }

    public List<Book> getAllByAuthorName(String authorName, Pageable pageable) {
        logger.trace("Try to get all books by author name");

        return bookRepository.findByAuthorName(authorName, pageable);
    }

    public List<Book> getAllByAuthorId(int authorId, Pageable pageable) {
        logger.trace("Try to get all books by author id");

        return bookRepository.findByAuthorId(authorId, pageable);
    }

    public List<Book> getAllByDate(Date date, Operator operator, Pageable pageable) {
        logger.trace("Try to get all books by date");

        return switch (operator) {
            case Operator.GREATER -> bookRepository.findByIssuedDateGreaterThan(date, pageable);
            case Operator.LESS -> bookRepository.findByIssuedDateLessThan(date, pageable);
            default -> throw new BadRequestException("Incorrect mode for selecting by date. Acceptable modes: more, less");
        };
    }

    public List<Book> getAllByRating(int rating, Operator operator, Pageable pageable) {
        logger.trace("Try to get all books by rating");

        return switch (operator) {
            case Operator.GREATER -> bookRepository.findByRatingGreaterThan(rating, pageable);
            case Operator.LESS -> bookRepository.findByRatingLessThan(rating, pageable);
            case Operator.EQUAL -> bookRepository.findByRating(rating, pageable);
        };
    }

    public List<Book> getByName(String name, Pageable pageable) {
        logger.trace("Try to get all books by name");

        return bookRepository.findByName(name, pageable);
    }

    public Optional<Book> getById(int id) {
        logger.trace("Try to find book by id");

        return bookRepository.findById(id);
    }

    public List<Book> getByStatusReading(int value, Operator operator, BookStatusScope scope, Pageable pageable) {
        logger.trace("Try to get books by status reading");

        return switch (operator) {
            case GREATER -> switch (scope) {

                case BookStatusScope.OVERALL -> bookStatusesRepository.findByStatusReadingOverallGreaterThan(value, pageable);
                case BookStatusScope.LAST_YEAR -> bookStatusesRepository.findByStatusReadingLastYearGreaterThan(value, pageable);
                case BookStatusScope.LAST_MONTH -> bookStatusesRepository.findByStatusReadingLastMonthGreaterThan(value, pageable);
                case BookStatusScope.LAST_WEEK -> bookStatusesRepository.findByStatusReadingLastWeekGreaterThan(value, pageable);

            };
            case LESS -> switch (scope) {

                case BookStatusScope.OVERALL -> bookStatusesRepository.findByStatusReadingOverallLessThan(value, pageable);
                case BookStatusScope.LAST_YEAR -> bookStatusesRepository.findByStatusReadingLastYearLessThan(value, pageable);
                case BookStatusScope.LAST_MONTH -> bookStatusesRepository.findByStatusReadingLastMonthLessThan(value, pageable);
                case BookStatusScope.LAST_WEEK -> bookStatusesRepository.findByStatusReadingLastWeekLessThan(value, pageable);

            };
            default -> throw new BadRequestException(ERROR_OPERATOR_MESSAGE);
        };

    }

    public List<Book> getByStatusRead(int value, Operator operator, BookStatusScope scope, Pageable pageable) {
        logger.trace("Try to get books by status read");

        return switch (operator) {
            case GREATER -> switch (scope) {

                case BookStatusScope.OVERALL -> bookStatusesRepository.findByStatusReadOverallGreaterThan(value, pageable);
                case BookStatusScope.LAST_YEAR -> bookStatusesRepository.findByStatusReadLastYearGreaterThan(value, pageable);
                case BookStatusScope.LAST_MONTH -> bookStatusesRepository.findByStatusReadLastMonthGreaterThan(value, pageable);
                case BookStatusScope.LAST_WEEK -> bookStatusesRepository.findByStatusReadLastWeekGreaterThan(value, pageable);

            };
            case LESS -> switch (scope) {

                case BookStatusScope.OVERALL -> bookStatusesRepository.findByStatusReadOverallLessThan(value, pageable);
                case BookStatusScope.LAST_YEAR -> bookStatusesRepository.findByStatusReadLastYearLessThan(value, pageable);
                case BookStatusScope.LAST_MONTH -> bookStatusesRepository.findByStatusReadLastMonthLessThan(value, pageable);
                case BookStatusScope.LAST_WEEK -> bookStatusesRepository.findByStatusReadLastWeekLessThan(value, pageable);

            };
            default -> throw new BadRequestException(ERROR_OPERATOR_MESSAGE);
        };

    }

    public List<Book> getByStatusDrop(int value, Operator operator, BookStatusScope scope, Pageable pageable) {
        logger.trace("Try to get books by status drop");

        return switch (operator) {
            case GREATER -> switch (scope) {

                case BookStatusScope.OVERALL -> bookStatusesRepository.findByStatusDropOverallGreaterThan(value, pageable);
                case BookStatusScope.LAST_YEAR -> bookStatusesRepository.findByStatusDropLastYearGreaterThan(value, pageable);
                case BookStatusScope.LAST_MONTH -> bookStatusesRepository.findByStatusDropLastMonthGreaterThan(value, pageable);
                case BookStatusScope.LAST_WEEK -> bookStatusesRepository.findByStatusDropLastWeekGreaterThan(value, pageable);

            };
            case LESS -> switch (scope) {

                case BookStatusScope.OVERALL -> bookStatusesRepository.findByStatusDropOverallLessThan(value, pageable);
                case BookStatusScope.LAST_YEAR -> bookStatusesRepository.findByStatusDropLastYearLessThan(value, pageable);
                case BookStatusScope.LAST_MONTH -> bookStatusesRepository.findByStatusDropLastMonthLessThan(value, pageable);
                case BookStatusScope.LAST_WEEK -> bookStatusesRepository.findByStatusDropLastWeekLessThan(value, pageable);

            };
            default -> throw new BadRequestException(ERROR_OPERATOR_MESSAGE);
        };

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



    public void addBookMetaData(BookDtoForCreating bookData) {
        logger.trace("Try to add book metadata");

        Optional<User> author = userRepository.findById(bookData.getAuthorId());
        if (author.isEmpty()) {
            throw new BadRequestException("Cannot add book, cause cannot find user/author with such id");
        }

        Book book = new Book();

        book.setAuthor(author.get());
        book.setName(bookData.getName());
        book.setIssuedDate(new Date());
        book.setGenres(bookData.getGenres());

        bookRepository.save(book);

        logger.trace("Book metadata was created successfully");
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
