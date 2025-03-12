package maksim.reviewsservice.repositories;

import java.util.Optional;
import maksim.reviewsservice.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findById(int id);

}
