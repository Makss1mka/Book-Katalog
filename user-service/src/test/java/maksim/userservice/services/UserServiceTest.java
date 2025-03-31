package maksim.userservice.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import maksim.userservice.config.AppConfig;
import maksim.userservice.exceptions.BadRequestException;
import maksim.userservice.exceptions.ConflictException;
import maksim.userservice.exceptions.NoContentException;
import maksim.userservice.exceptions.NotFoundException;
import maksim.userservice.models.dtos.*;
import maksim.userservice.models.entities.Book;
import maksim.userservice.models.entities.User;
import maksim.userservice.models.entities.UserBookStatuses;
import maksim.userservice.repositories.UserRepository;
import maksim.userservice.utils.enums.BookStatus;
import maksim.userservice.utils.enums.JoinMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
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

    @InjectMocks
    private UserService userService;

    private User user;
    private Book book;
    private UserBookStatuses userBookStatuses;
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

        userBookStatuses = new UserBookStatuses();
        userBookStatuses.setId(1);
        userBookStatuses.setBook(book);
        userBookStatuses.setStatus("READ");
        userBookStatuses.setLike(false);

        user.setBookStatuses(new ArrayList<>(List.of(userBookStatuses)));

        userDto = new UserDto(user, JoinMode.WITHOUT);

        MockitoAnnotations.openMocks(this);
    }



    @Test
    void testGetUserById_Success() {
        UserDto result;

        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        result = userService.getUserById(1, JoinMode.WITH_STATUSES_AND_BOOKS);

        assertEquals(userDto.getId(), result.getId());
        assertEquals(userDto.getName(), result.getName());

        result = userService.getUserById(1, JoinMode.WITHOUT);

        assertEquals(userDto.getId(), result.getId());
        assertEquals(userDto.getName(), result.getName());

        verify(userRepository, times(1)).findByIdWithJoinStatusesAndBooks(1);
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    void testGetUserById_NotFoundException() {
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
            userService.getUserById(1, JoinMode.WITHOUT)
        );

        verify(userRepository, times(1)).findById(anyInt());
    }




    @Test
    void testGetAllBooksByUserStatus_Success() {
        List<UserBookStatuses> statuses = Arrays.asList(userBookStatuses, userBookStatuses);

        when(userRepository.findAllBooksByUserStatus(anyInt(), anyString(), any(Pageable.class))).thenReturn(statuses);

        List<BookDto> result = userService.getAllBooksByUserStatus(1, BookStatus.READ, PageRequest.of(0, 10));

        assertEquals(2, result.size());

        verify(userRepository, times(1)).findAllBooksByUserStatus(anyInt(), anyString(), any(Pageable.class));
    }

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





    @Test
    void testCreateStatus_Success() {
        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));
        when(restTemplate.getForEntity(anyString(), any())).thenReturn(new ResponseEntity<>(book, HttpStatus.OK));
        when(appConfig.getBookServiceUrl()).thenReturn("url");

        UserDto result = userService.createStatus(1, new CreateBookStatusDto(2, "READ"));

        assertEquals(2, result.getBookStatuses().size());
        assertEquals(result.getId(), user.getId());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateStatus_BookIsInStatus() {
        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));

        Executable executable = () -> userService.createStatus(1, new CreateBookStatusDto(1, "READ"));

        assertThrows(ConflictException.class, executable);
    }

    @Test
    void testCreateStatus_BookNotFound() {
        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));
        when(restTemplate.getForEntity(anyString(), any())).thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        when(appConfig.getBookServiceUrl()).thenReturn("url");

        Executable executable = () -> userService.createStatus(1, new CreateBookStatusDto(2, "READ"));

        assertThrows(ConflictException.class, executable);
    }





    @Test
    void testUpdateStatus_SuccessStatusUpdate() {
        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UpdateBookStatusDto updateDto = new UpdateBookStatusDto(1, "READ", true);

        UserDto result = userService.updateStatus(1, updateDto);

        assertEquals(BookStatus.READ, result.getBookStatuses().get(0).getStatus());
    }

    @Test
    void testUpdateStatus_SuccessLikeUpdate() {
        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UpdateBookStatusDto updateDto = new UpdateBookStatusDto(1, "LIKED", true);

        UserDto result = userService.updateStatus(1, updateDto);

        assertTrue(result.getBookStatuses().get(0).getLike());
    }

    @Test
    void testUpdateStatus_StatusNotFound() {
        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));

        UpdateBookStatusDto updateDto = new UpdateBookStatusDto(999, "READ", true);

        assertThrows(NotFoundException.class, () -> {
            userService.updateStatus(1, updateDto);
        });
    }

    @Test
    void testUpdateStatus_NoContentException() {
        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));

        UpdateBookStatusDto updateDto = new UpdateBookStatusDto(1, "READING", false);

        Executable executable = () -> userService.updateStatus(1, updateDto);

        assertThrows(ConflictException.class, executable);
    }

    @Test
    void testUpdateStatus_StatusAny() {
        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));

        UpdateBookStatusDto updateDto = new UpdateBookStatusDto(1, "ANY", false);

        Executable executable = () -> userService.updateStatus(1, updateDto);

        assertThrows(ConflictException.class, executable);
    }





    @Test
    void testUpdateUser_SuccessNameChange() {
        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setNewName("New Name");

        UserDto result = userService.updateUser(1, updateDto);

        assertEquals("New Name", result.getName());
    }

    @Test
    void testUpdateUser_SuccessEmailChange() {
        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setNewEmail("New Email");

        UserDto result = userService.updateUser(1, updateDto);

        assertEquals("New Email", result.getEmail());
    }

    @Test
    void testUpdateUser_SuccessPasswordChange() {
        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setOldPassword("oldPassword");
        updateDto.setNewPassword("newPassword");

        userService.updateUser(1, updateDto);

        verify(passwordEncoder, times(1)).encode("newPassword");
    }

    @Test
    void testUpdateUser_PasswordMismatch() {
        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setOldPassword("wrongPassword");
        updateDto.setNewPassword("newPassword");

        assertThrows(BadRequestException.class, () -> {
            userService.updateUser(1, updateDto);
        });
    }

    @Test
    void testUpdateUser_PasswordEquals() {
        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        UpdateUserDto updateDto = new UpdateUserDto();
        updateDto.setOldPassword("newPassword");
        updateDto.setNewPassword("newPassword");

        assertThrows(BadRequestException.class, () -> {
            userService.updateUser(1, updateDto);
        });
    }

    @Test
    void testUpdateUser_NoContent() {
        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));

        assertThrows(NoContentException.class, () -> {
            userService.updateUser(1, new UpdateUserDto());
        });
    }





    @Test
    void testDeleteStatusEntity_Success() {
        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.deleteStatusEntity(1, 1);

        assertTrue(result.getBookStatuses().isEmpty());
    }

    @Test
    void testDeleteStatusEntity_NotFound() {
        when(userRepository.findByIdWithJoinStatusesAndBooks(1)).thenReturn(Optional.of(user));

        assertThrows(NotFoundException.class, () -> {
            userService.deleteStatusEntity(1, 999);
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

