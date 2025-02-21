package maksim.bookservice.repositories;

import maksim.bookservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}
