package maksim.auth_service.repositories;

import maksim.auth_service.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByName(String name);
    Optional<User> findByEmail(String email);
    Optional<User> findByNameAndEmail(String name, String email);
    Optional<User> findByNameAndPassword(String name, String password);
    Optional<User> findByEmailAndPassword(String email, String password);
}
