package maksim.auth_service.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import jakarta.validation.constraints.NotNull;
import maksim.auth_service.config.AppConfig;

import maksim.auth_service.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

@Service
public class JwtTokenService {
    private final static Logger logger = LoggerFactory.getLogger(JwtTokenService.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AppConfig appConfig;

    public String generateToken(@NotNull String username, @NotNull String email,
                                @NotNull String role, int id, int expirationTime) {
        logger.trace("Start creating token from plain values:" +
                "\n\t\t\tusername - {}" +
                "\n\t\t\temail - {}" +
                "\n\t\t\ttole - {}" +
                "\n\t\t\tid - {}" +
                "\n\t\t\texpTime - {}", username, email, role, id, expirationTime);

        HashMap<String, Object> claims = new HashMap<String, Object>();
        claims.put("email", email);
        claims.put("role", role);

        String token = Jwts.builder()
                .setId(String.valueOf(id))
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS256, appConfig.getSecret())
                .compact();

        logger.trace("Token from plain vals was created - {}", token);

        return token;
    }

    public String generateToken(@NotNull User user, int expirationTime) {
        logger.trace("Start creating token from user object:" +
                "\n\t\t\tusername - {}" +
                "\n\t\t\temail - {}" +
                "\n\t\t\ttole - {}" +
                "\n\t\t\tid - {}" +
                "\n\t\t\texpTime - {}", user.getName(), user.getEmail(), user.getRole(), user.getId(), expirationTime);

        HashMap<String, Object> claims = new HashMap<String, Object>();
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole());

        String token = Jwts.builder()
                .setId(String.valueOf(user.getId()))
                .setSubject(user.getName())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS256, appConfig.getSecret())
                .compact();

        logger.trace("Token from userObj was created - {}", token);

        return token;
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .setSigningKey(appConfig.getSecret())
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractEmail(String token) {
        return extractClaim(token, (Claims claims) -> claims.get("email") ).toString();
    }

    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    public int extractId(String token) {
        return Integer.parseInt(extractClaim(token, Claims::getId));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpirationTime(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token) {
        return extractExpirationTime(token).before(new Date());
    }

    public boolean validateToken(String token, User user) {
        return (!isTokenExpired(token)
                && extractUsername(token).equals(user.getName())
                && extractEmail(token).equals(user.getEmail()));
    }

}
