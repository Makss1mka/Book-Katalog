package maksim.authservice.repositories;

import maksim.authservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findById(int id);
    Optional<User> findByEmailAndPassword(String email, String password);
    Optional<User> findByNameAndPassword(String email, String password);
    Optional<User> findByNameOrEmail(String name, String email);
    Optional<User> findByName(String name);
    Optional<User> findByEmail(String email);

}
