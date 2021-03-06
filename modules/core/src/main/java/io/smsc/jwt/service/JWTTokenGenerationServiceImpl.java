package io.smsc.jwt.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.smsc.jwt.model.JWTUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service class used for generating and processing access and refresh tokens.
 *
 * @author Nazar Lipkovskyy
 * @see JWTTokenGenerationService
 * @see io.jsonwebtoken.Claims;
 * @see io.jsonwebtoken.Jwts;
 * @see io.jsonwebtoken.SignatureAlgorithm;
 * @since 0.0.1-SNAPSHOT
 */
@Service
public class JWTTokenGenerationServiceImpl implements JWTTokenGenerationService {

    private static final Logger LOG = LoggerFactory.getLogger(JWTTokenGenerationServiceImpl.class);

    public static final long serialVersionUID = -3301605591108950415L;

    public static final String TOKEN_EXCEPTION_MESSAGE = "Token is empty or null";

    public static final String CLAIM_KEY_USERNAME = "sub";
    public static final String CLAIM_KEY_CREATED = "created";

    /**
     * This string is used as a name of request header which contains tokens
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * This digit is used as a value of access token life duration in seconds
     */
    @Value("${jwt.expiration}")
    private Long expiration;

    @Override
    public String getUsernameFromToken(String token) {
        String username = null;
        try {
            Claims claims = getClaimsFromToken(token);
            if(claims != null) {
                username = claims.getSubject();
            }
        } catch (Exception e) {
            LOG.debug(TOKEN_EXCEPTION_MESSAGE, e);
        }
        return username;
    }

    private Date getExpirationDateFromToken(String token) {
        Date expirationDate = null;
        Claims claims;
        try {
            claims = getClaimsFromToken(token);
            if(claims != null) {
                expirationDate = claims.getExpiration();
            }
        } catch (Exception e) {
            LOG.debug(TOKEN_EXCEPTION_MESSAGE, e);
        }
        return expirationDate;
    }

    private Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            LOG.debug(TOKEN_EXCEPTION_MESSAGE, e);
            claims = null;
        }
        return claims;
    }

    private Date generateExpirationDateForAccessToken() {
        return new Date(System.currentTimeMillis() + expiration * 1000);
    }

    private Date generateExpirationDateForRefreshToken() {
        return new Date(System.currentTimeMillis() + expiration * 24 * 1000);
    }

    private Boolean isTokenExpired(String token) {
        Date expirationDate;
        try {
            expirationDate = getExpirationDateFromToken(token);
            if(expirationDate != null) {
                return expirationDate.before(new Date());
            }
        }
        catch (Exception e) {
            LOG.debug(TOKEN_EXCEPTION_MESSAGE, e);
        }
        return true;
    }

    @Override
    public String refreshToken(String token) {
        String refreshedToken = null;
        Claims claims;
        try {
            claims = getClaimsFromToken(token);
            if(claims != null) {
                claims.put(CLAIM_KEY_CREATED, new Date());
                refreshedToken = generateAccessToken(claims);
            }
        } catch (Exception e) {
            LOG.debug(TOKEN_EXCEPTION_MESSAGE, e);
            refreshedToken = null;
        }
        return refreshedToken;
    }

    @Override
    public Boolean validateToken(String token, UserDetails userDetails) {
        JWTUser user = (JWTUser) userDetails;
        final String username = getUsernameFromToken(token);
        return username.equals(user.getUsername()) && !isTokenExpired(token);
    }

    @Override
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USERNAME, userDetails.getUsername());
        claims.put(CLAIM_KEY_CREATED, new Date());
        return generateAccessToken(claims);
    }

    @Override
    public String generateAccessToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateExpirationDateForAccessToken())
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    @Override
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USERNAME, userDetails.getUsername());
        claims.put(CLAIM_KEY_CREATED, new Date());
        return generateRefreshToken(claims);
    }

    @Override
    public String generateRefreshToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateExpirationDateForRefreshToken())
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
}
