package maksim.userservice.repositories;

import maksim.userservice.models.entities.UserBookStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBookStatusRepository extends JpaRepository<UserBookStatus, Integer> {
}
