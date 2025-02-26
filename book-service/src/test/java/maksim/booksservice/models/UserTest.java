package maksim.booksservice.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    private User user;
    private final Date date = new Date();

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setName("John Doe");
        user.setProfilePicPath("/images/johndoe.png");
        user.setEmail("johndoe@example.com");
        user.setRole("USER");
        user.setRegistrationDate(date);
        user.setPassword("password123");
    }

    @Test
    void testGetters() {
        assertEquals(1, user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("/images/johndoe.png", user.getProfilePicPath());
        assertEquals("johndoe@example.com", user.getEmail());
        assertEquals("USER", user.getRole());
        assertEquals(date, user.getRegistrationDate());
        assertEquals("password123", user.getPassword());
    }

    @Test
    void testSetters() {
        user.setId(2);
        assertEquals(2, user.getId());

        user.setName("Jane Doe");
        assertEquals("Jane Doe", user.getName());

        user.setProfilePicPath("/images/janedoe.png");
        assertEquals("/images/janedoe.png", user.getProfilePicPath());

        user.setEmail("janedoe@example.com");
        assertEquals("janedoe@example.com", user.getEmail());

        user.setRole("ADMIN");
        assertEquals("ADMIN", user.getRole());

        Date newDate = new Date();
        user.setRegistrationDate(newDate);
        assertEquals(newDate, user.getRegistrationDate());

        user.setPassword("newpassword123");
        assertEquals("newpassword123", user.getPassword());
    }
}