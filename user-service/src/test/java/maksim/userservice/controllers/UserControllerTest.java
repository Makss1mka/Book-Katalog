package maksim.userservice.controllers;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import maksim.userservice.models.dtos.*;
import maksim.userservice.models.entities.Book;
import maksim.userservice.services.UserService;
import maksim.userservice.utils.enums.BookStatus;
import maksim.userservice.utils.validators.CreateUserDtoValidators;
import maksim.userservice.utils.validators.UpdateUserDtoValidators;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Collections;
import java.util.List;

class UserControllerTest {
    @Mock
    private UserService userService;

    @Mock
    private CreateUserDtoValidators createUserDtoValidators;

    @Mock
    private UpdateUserDtoValidators updateUserDtoValidators;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void testGetAllBooksByUserStatus_Success() throws Exception {
        BookDto bookDto = new BookDto(new Book(), BookStatus.READ);

        when(userService.getAllBooksByUserStatus(anyInt(), any(), any())).thenReturn(List.of(bookDto));

        mockMvc.perform(get("/api/v1/users/1/books")
                        .param("status", "READ")
                        .param("pageNum", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists());
    }

    @Test
    void testGetAllBooksByUserStatus_EmptyList() throws Exception {
        when(userService.getAllBooksByUserStatus(anyInt(), any(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/users/1/books")
                        .param("status", "READ")
                        .param("pageNum", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testGetUserById_Success() throws Exception {
        UserDto userDto = new UserDto();

        when(userService.getUserById(anyInt(), any())).thenReturn(userDto);

        mockMvc.perform(get("/api/v1/users/1")
                        .param("joinMode", "with_statuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testCreateUser_Success() throws Exception {
        CreateUserDto createUserDto = new CreateUserDto();
        createUserDto.setEmail("test@test.com");
        createUserDto.setName("Test User");
        createUserDto.setPassword("password");

        UserDto userDto = new UserDto();

        when(createUserDtoValidators.isValid(any())).thenReturn(true);
        when(userService.createUser(any())).thenReturn(userDto);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.com\",\"name\":\"Test User\",\"password\":\"password\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testCreateUser_InvalidData() throws Exception {
        when(createUserDtoValidators.isValid(any())).thenReturn(false);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invalid\",\"name\":\"Test User\",\"password\":\"password\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateStatus_Success() throws Exception {
        when(userService.createStatus(anyInt(), any())).thenReturn(new UserDto());

        mockMvc.perform(post("/api/v1/users/1/book-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":1,\"status\":\"READ\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testUpdateUser_Success() throws Exception {
        when(updateUserDtoValidators.isValid(any())).thenReturn(true);
        when(userService.updateUser(anyInt(), any(UpdateUserDto.class))).thenReturn(new UserDto());

        mockMvc.perform(patch("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newName\":\"New Name\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testUpdateStatus_Success() throws Exception {
        when(userService.updateStatus(anyInt(), any())).thenReturn(new UserDto());

        mockMvc.perform(patch("/api/v1/users/1/book-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":1,\"status\":\"READ\",\"statusValue\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testDeleteUser_Success() throws Exception {
        doNothing().when(userService).deleteUser(anyInt());

        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isCreated())
                .andExpect(content().string("User was successfully deleted"));
    }

    @Test
    void testDeleteStatus_Success() throws Exception {
        UserDto userDto = new UserDto();
        when(userService.deleteStatusEntity(anyInt(), anyInt())).thenReturn(userDto);

        mockMvc.perform(delete("/api/v1/users/1/book-status/1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").exists());
    }


    @Test
    void testCreateStatus_InvalidBookId() throws Exception {
        mockMvc.perform(post("/api/v1/users/1/book-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":-1,\"status\":\"READ\"}"))
                .andExpect(status().isBadRequest());
    }
}