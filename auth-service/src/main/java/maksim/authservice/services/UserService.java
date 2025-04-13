package maksim.authservice.services;

import maksim.authservice.exceptions.BadRequestException;
import maksim.authservice.exceptions.NotFoundException;
import maksim.authservice.exceptions.UnauthorizedException;
import maksim.authservice.models.UserDto;
import maksim.authservice.models.User;
import maksim.authservice.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    UserService(
            UserRepository userRepository,
            JwtTokenService jwtTokenService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public String authUser(String token, UserDto userDto) {
        String newToken;

        if (token != null) {
            Optional<User> user = userRepository.findById(jwtTokenService.extractId(token));

            if(user.isEmpty()) {
                throw new NotFoundException("Cannot find such user");
            } else if (jwtTokenService.validateToken(token, user.get())) {
                newToken = jwtTokenService.generateToken(user.get(), 3600000 * 2);
            } else {
                throw new UnauthorizedException("Token is not valid");
            }
        } else if(userDto.getName() != null && userDto.getPassword() != null) {
            Optional<User> user = userRepository.findByName(userDto.getName());

            if (user.isEmpty()) {
                throw new NotFoundException("Cannot find such user");
            }

            if (!passwordEncoder.matches(userDto.getPassword(), user.get().getPassword())) {
                throw new UnauthorizedException("Invalid creds for login");
            }

            newToken = jwtTokenService.generateToken(user.get(), 3600000 * 2);
        } else if(userDto.getEmail() != null && userDto.getPassword() != null) {
            Optional<User> user = userRepository.findByEmail(userDto.getEmail());

            if(user.isEmpty()) {
                throw new NotFoundException("Cannot find such user");
            }

            if (!passwordEncoder.matches(userDto.getPassword(), user.get().getPassword())) {
                throw new UnauthorizedException("Invalid creds for login");
            }

            newToken = jwtTokenService.generateToken(user.get(), 3600000 * 2);
        } else {
            throw new BadRequestException("Check your request throwout");
        }

        return newToken;
    }

}
