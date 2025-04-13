package maksim.authservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import maksim.authservice.models.UserDto;
import maksim.authservice.services.JwtTokenService;
import maksim.authservice.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1")
public class AuthController {
    private final static Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final JwtTokenService jwtTokenService;
    private final UserService userService;

    @Autowired
    public AuthController(
            JwtTokenService jwtTokenService,
            UserService userService
    ) {
        this.jwtTokenService = jwtTokenService;
        this.userService = userService;
    }

    @PostMapping("/auth")
    public ResponseEntity<String> login(
            HttpServletRequest request,
            @RequestBody UserDto userDto
    ) {
        logger.trace("Try to login");

        String authHeader = request.getHeader("Authorization");
        String oldToken = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7) : null;

        String token = userService.authUser(oldToken, userDto);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", String.format("Bearer %s", token));

        logger.trace("Login successfully");

        return new ResponseEntity<>(headers, HttpStatus.OK);
    }

}


