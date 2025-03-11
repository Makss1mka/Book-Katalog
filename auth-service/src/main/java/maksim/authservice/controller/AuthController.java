package maksim.authservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import maksim.authservice.models.User;
import maksim.authservice.services.JwtTokenService;
import maksim.authservice.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1")
public class AuthController {
    private final static Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenService jwtTokenService;

    @GetMapping("/auth")
    public ResponseEntity<?> login(HttpServletRequest request,
                                   @RequestParam(required = false) String username,
                                   @RequestParam(required = false) String password,
                                   @RequestParam(required = false) String email) {
        logger.trace("Try to login:");

        String authHeader = request.getHeader("Authorization");
        HttpHeaders headers = new HttpHeaders();

        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            logger.trace("\tTry to login via token");

            String token = authHeader.substring(7);

            logger.trace("\tToken - {}", token);

            Optional<User> userData = userService.findById(jwtTokenService.extractId(token));

            if(userData.isPresent() && jwtTokenService.validateToken(token, userData.get())) {
                logger.trace("""
                        \tToken is valid
                        \tUser data - {} {} {}
                        """,
                        userData.get().getId(),
                        userData.get().getName(),
                        userData.get().getEmail());

                headers.add("Authorization", String.format("Bearer %s",
                        jwtTokenService.generateToken(userData.get(), 3600000 * 2)));

                return new ResponseEntity<>(userData, headers, HttpStatus.OK);
            } else if(userData.isPresent()) {
                logger.trace("""
                        \tToken is not valid\
                        \tUsername from: obj {} - token {}\
                        \tEmail from: obj {} - token {}\
                        \tId from: obj {} - token {}\
                        \tExp time {}
                        """,
                        userData.get().getName(), jwtTokenService.extractUsername(token),
                        userData.get().getEmail(), jwtTokenService.extractEmail(token),
                        userData.get().getId(), jwtTokenService.extractId(token),
                        jwtTokenService.extractExpirationTime(token));

                return new ResponseEntity<>("Token is not valid", HttpStatus.UNAUTHORIZED);
            } else {
                logger.trace("\tToken is not valid. Cannot get userData from token's id");

                return new ResponseEntity<>("Token is not valid", HttpStatus.UNAUTHORIZED);
            }

        } else if(username != null && password != null) {
            logger.trace("\tTry to login via name and pas ({}, {})", username, password);

            Optional<User> userData = userService.findByNameAndPassword(username, password);

            if(userData.isPresent()) {
                logger.trace("\tFind user with such username and password");

                headers.add("Authorization", String.format("Bearer %s",
                        jwtTokenService.generateToken(userData.get(), 3600000 * 2)));

                return new ResponseEntity<>(userData.get(), headers, HttpStatus.OK);
            } else {
                logger.trace("\tCannot find user with such username and password");

                return new ResponseEntity<>("Invalid creds for login", HttpStatus.BAD_REQUEST);
            }

        } else if(email != null && password != null) {
            logger.trace("\tTry to login via email and pas ({}, {})", email, password);

            Optional<User> userData = userService.findByEmailAndPassword(email, password);

            if(userData.isPresent()) {
                logger.trace("\tFind user with such email and password");

                headers.add("Authorization", String.format("Bearer %s",
                        jwtTokenService.generateToken(userData.get(), 3600000 * 2)));

                return new ResponseEntity<>(userData.get(), headers, HttpStatus.OK);
            } else {
                logger.trace("\tCannot find user with such email and password");

                return new ResponseEntity<>("Invalid creds for login", HttpStatus.BAD_REQUEST);
            }

        }

        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/reg")
    public ResponseEntity<?> registration(@RequestParam(required = true) String username,
                                          @RequestParam(required = true) String password,
                                          @RequestParam(required = true) String email) {

        HttpHeaders headers = new HttpHeaders();

        logger.trace("New user (name - {} ; password - {} ; email - {}) try to register", username, password, email);

        if(userService.findByNameOrEmail(username, email).isEmpty()) {
            User newUserData = userService.create(username, password, email);

            String token = jwtTokenService.generateToken(newUserData, 3600000 * 2);

            logger.trace("\tSuccessfully. User was created.\n\tHis token {}", token);

            headers.add("Content-Type", "application/json");
            headers.add("Authorization", String.format("Bearer %s", token));

            return new ResponseEntity<>(newUserData, headers, HttpStatus.CREATED);
        } else {
            logger.trace("\tUnsuccessfully. User with such creds (name - {}, email - {}) already exist", username, email);

            return new ResponseEntity<>("Username or email have already taken", HttpStatus.BAD_REQUEST);
        }

    }

}


