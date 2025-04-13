package maksim.authservice.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import maksim.authservice.config.AppConfig;

import maksim.authservice.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

@Service
public class JwtTokenService {
    private final static Logger logger = LoggerFactory.getLogger(JwtTokenService.class);

    private final AppConfig appConfig;

    @Autowired
    public JwtTokenService(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public String generateToken(String username, String email, String role, int id, int expirationTime) {
        logger.trace("Start creating token from plain values:" +
                "username - {} ; " +
                "email - {} ; " +
                "tole - {} ; " +
                "id - {} ; " +
                "expTime - {}", username, email, role, id, expirationTime);

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

    public String generateToken(User user, int expirationTime) {
        logger.trace("Start creating token");

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

    public boolean validateToken(String token, int id, String name, String email) {
        return (!isTokenExpired(token)
                && extractId(token) == id
                && extractUsername(token).equals(name)
                && extractEmail(token).equals(email));
    }

    public boolean validateToken(String token, User user) {
        return (!isTokenExpired(token)
                && extractId(token) == user.getId()
                && extractUsername(token).equals(user.getName())
                && extractEmail(token).equals(user.getEmail()));
    }

}
