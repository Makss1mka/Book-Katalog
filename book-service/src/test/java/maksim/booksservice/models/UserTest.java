package maksim.booksservice.models;

import maksim.booksservice.models.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setName("John Doe");
        user.setProfilePicPath("/images/johndoe.png");
        user.setEmail("johndoe@example.com");
    }

    @Test
    void testGetters() {
        assertEquals(1, user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("/images/johndoe.png", user.getProfilePicPath());
        assertEquals("johndoe@example.com", user.getEmail());
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
    }
}