package maksim.auth_service.services;

import jakarta.validation.constraints.NotNull;
import maksim.auth_service.models.User;
import maksim.auth_service.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> findAll(@NotNull Sort sort) {
        return userRepository.findAll(sort);
    }

    public Optional<User> findById(int id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByName(String name) {
        return userRepository.findByName(name);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByNameAndEmail(String name, String email) {
        return userRepository.findByNameAndEmail(name, email);
    }

    public Optional<User> findByNameAndPassword(String name, String password) {
        return userRepository.findByNameAndPassword(name, password);
    }

    public Optional<User> findByEmailAndPassword(String email, String password) {
        return userRepository.findByEmailAndPassword(email, password);
    }

    public Page<User> findPage(int pageNum, int pageAmount, @NotNull Sort sort) {
        return userRepository.findAll(PageRequest.of(pageNum, pageAmount, sort));
    }

    public User create(User user) {
        return userRepository.save(user);
    }

    public void deleteById(int id) {
        userRepository.deleteById(id);
    }

}
