package maksim.userservice.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import maksim.userservice.config.AppConfig;
import maksim.userservice.exceptions.BadRequestException;
import maksim.userservice.exceptions.ConflictException;
import maksim.userservice.exceptions.NoContentException;
import maksim.userservice.exceptions.NotFoundException;
import maksim.userservice.models.dtos.crud.CreateBookStatusDto;
import maksim.userservice.models.dtos.crud.CreateUserDto;
import maksim.userservice.models.dtos.crud.UpdateUserDto;
import maksim.kafkaclient.dtos.CreateStatusKafkaDto;
import maksim.kafkaclient.dtos.DeleteStatusKafkaDto;
import maksim.userservice.models.dtos.result.BookDto;
import maksim.userservice.models.dtos.result.UserDto;
import maksim.userservice.models.entities.Book;
import maksim.userservice.models.entities.User;
import maksim.userservice.models.entities.UserBookStatus;
import maksim.userservice.repositories.UserRepository;
import maksim.userservice.services.kafka.producers.LikeEventsProducer;
import maksim.userservice.services.kafka.producers.StatusEventsProducer;
import maksim.userservice.utils.enums.BookStatus;
import maksim.userservice.utils.enums.JoinMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

class UserServiceTest {
    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AppConfig appConfig;

    @Mock
    private LikeEventsProducer likeEventsProducer;

    @Mock
    private StatusEventsProducer statusEventsProducer;

    @InjectMocks
    private UserService userService;

    private User user;
    private Book book;
    private UserBookStatus userBookStatus;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setEmail("email");
        user.setName("name");

        book = new Book();
        book.setId(1);
        book.setName("book");

        userBookStatus = new UserBookStatus();
        userBookStatus.setId(1);
        userBookStatus.setBook(book);
        userBookStatus.setStatus("READ");

        user.setBookStatuses(new ArrayList<>(List.of(userBookStatus)));

        userDto = new UserDto(user, JoinMode.WITHOUT);

        MockitoAnnotations.openMocks(this);
    }

//    @Test
//    void testGetUserById_Success() {
//        UserDto result;
//
//        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));
//        when(userRepository.findById(1)).thenReturn(Optional.of(user));
//
//        result = userService.getUserById(1, JoinMode.WITH_STATUSES_AND_BOOKS);
//
//        assertEquals(userDto.getId(), result.getId());
//        assertEquals(userDto.getName(), result.getName());
//
//        result = userService.getUserById(1, JoinMode.WITHOUT);
//
//        assertEquals(userDto.getId(), result.getId());
//        assertEquals(userDto.getName(), result.getName());
//
//        verify(userRepository, times(1)).findByIdWithJoinStatusesAndBooks(1);
//        verify(userRepository, times(1)).findById(1);
//    }

    @Test
    void testGetUserById_NotFoundException() {
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
            userService.getUserById(1, JoinMode.WITHOUT)
        );

        verify(userRepository, times(1)).findById(anyInt());
    }

//    @Test
//    void testGetAllBooksByUserStatus_Success() {
//        List<UserBookStatus> statuses = Arrays.asList(userBookStatus, userBookStatus);
//
//        when(userRepository.findAllBooksByUserStatus(anyInt(), anyString(), any(Pageable.class))).thenReturn(statuses);
//
//        List<BookDto> result = userService.getAllBooksByUserStatus(1, BookStatus.READ, PageRequest.of(0, 10));
//
//        assertEquals(2, result.size());
//
//        verify(userRepository, times(1)).findAllBooksByUserStatus(anyInt(), anyString(), any(Pageable.class));
//    }

    @Test
    void testGetAllBooksByUserStatus_EmptyList() {
        when(userRepository.findAllBooksByUserStatus(anyInt(), anyString(), any(Pageable.class)))
            .thenReturn(new ArrayList<>());

        List<BookDto> result = userService.getAllBooksByUserStatus(1, BookStatus.READ, PageRequest.of(0, 10));

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findAllBooksByUserStatus(anyInt(), anyString(), any(Pageable.class));
    }

    @Test
    void testCreateUser_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        CreateUserDto createUserDto = new CreateUserDto();
        createUserDto.setEmail("some");
        createUserDto.setName("some");
        createUserDto.setPassword("some");

        UserDto result = userService.createUser(createUserDto);

        assertEquals(result.getName(), createUserDto.getName());
        assertEquals(result.getEmail(), createUserDto.getEmail());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUser_ConflictOnEmail() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        CreateUserDto createUserDto = new CreateUserDto();
        createUserDto.setEmail("some");

        assertThrows(ConflictException.class, () -> {
            userService.createUser(createUserDto);
        });

        verify(userRepository, times(1)).findByEmail(anyString());
    }

    @Test
    void testCreateUser_ConflictOnName() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByName(anyString())).thenReturn(Optional.of(user));

        CreateUserDto createUserDto = new CreateUserDto();
        createUserDto.setName("some");

        assertThrows(ConflictException.class, () -> {
            userService.createUser(createUserDto);
        });

        verify(userRepository, times(1)).findByName(anyString());
    }

//    @Test
//    void testCreateStatus_Success() {
//        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));
//        when(restTemplate.getForEntity(anyString(), any())).thenReturn(new ResponseEntity<>(book, HttpStatus.OK));
//        when(appConfig.getBookServiceUrl()).thenReturn("url");
//        doNothing().when(statusEventsProducer).publishStatusCreate(any(CreateStatusKafkaDto.class));
//
//        UserDto result = userService.createStatus(1, new CreateBookStatusDto(2, "READ"));
//
//        assertEquals(2, result.getBookStatuses().size());
//        assertEquals(result.getId(), user.getId());
//
//        verify(userRepository, times(1)).save(any(User.class));
//    }

    @Test
    void testCreateStatus_BookIsInStatus() {
        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));

        assertThrows(ConflictException.class, () ->
            userService.createStatus(1, new CreateBookStatusDto(1, "READ"))
        );
    }

    @Test
    void testCreateStatus_BookNotFound() {
        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));
        when(restTemplate.getForEntity(anyString(), any())).thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        when(appConfig.getBookServiceUrl()).thenReturn("url");

        assertThrows(NotFoundException.class, () ->
            userService.createStatus(1, new CreateBookStatusDto(2, "READ"))
        );
    }


//    @Test
//    void testUpdateUser_SuccessNameChange() {
//        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));
//        when(userRepository.save(any(User.class))).thenReturn(user);
//
//        UpdateUserDto updateDto = new UpdateUserDto();
//        updateDto.setNewName("New Name");
//
//        UserDto result = userService.updateUser(1, updateDto);
//
//        assertEquals("New Name", result.getName());
//    }

//    @Test
//    void testUpdateUser_SuccessEmailChange() {
//        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));
//        when(userRepository.save(any(User.class))).thenReturn(user);
//
//        UpdateUserDto updateDto = new UpdateUserDto();
//        updateDto.setNewEmail("New Email");
//
//        UserDto result = userService.updateUser(1, updateDto);
//
//        assertEquals("New Email", result.getEmail());
//    }

//    @Test
//    void testUpdateUser_SuccessPasswordChange() {
//        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));
//        when(passwordEncoder.matches(any(), any())).thenReturn(true);
//        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
//        when(userRepository.save(any(User.class))).thenReturn(user);
//
//        UpdateUserDto updateDto = new UpdateUserDto();
//        updateDto.setOldPassword("oldPassword");
//        updateDto.setNewPassword("newPassword");
//
//        userService.updateUser(1, updateDto);
//
//        verify(passwordEncoder, times(1)).encode("newPassword");
//    }

    @Test
    void testUpdateUser_PasswordMismatch() {
        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setOldPassword("wrongPassword");
        updateDto.setNewPassword("newPassword");

        assertThrows(BadRequestException.class, () ->
            userService.updateUser(1, updateDto)
        );
    }

    @Test
    void testUpdateUser_PasswordEquals() {
        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setOldPassword("newPassword");
        updateDto.setNewPassword("newPassword");

        assertThrows(BadRequestException.class, () ->
            userService.updateUser(1, updateDto)
        );
    }

    @Test
    void testUpdateUser_NoContent() {
        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));

        assertThrows(NoContentException.class, () ->
            userService.updateUser(1, new UpdateUserDto())
        );
    }

//    @Test
//    void testDeleteStatus_Success() {
//        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));
//        when(userRepository.save(any(User.class))).thenReturn(user);
//        doNothing().when(statusEventsProducer).publishStatusDelete(any(DeleteStatusKafkaDto.class));
//
//        UserDto result = userService.deleteStatus(1, 1);
//
//        assertTrue(result.getBookStatuses().isEmpty());
//    }

    @Test
    void testDeleteStatus_NotFound() {
        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));

        assertThrows(NotFoundException.class, () -> {
            userService.deleteStatus(1, 999);
        });
    }

    @Test
    void testDeleteUser_Success() {
        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(any(User.class));

        userService.deleteUser(1);

        verify(userRepository, times(1)).delete(any(User.class));
    }

}

