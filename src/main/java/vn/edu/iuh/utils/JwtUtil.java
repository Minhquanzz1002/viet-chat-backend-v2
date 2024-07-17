package vn.edu.iuh.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import vn.edu.iuh.config.AppProperties;
import vn.edu.iuh.security.UserPrincipal;
import vn.edu.iuh.utils.enums.JwtType;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtil {
    private final AppProperties appProperties;
    public String generateRegisterToken(UserDetails userDetails) {
        return buildToken(userDetails, appProperties.getAuth().getRegisterTokenExpirationMilliseconds(), JwtType.REGISTER_TOKEN);
    }
    public String generateAccessToken(UserDetails userDetails) {
        return buildToken(userDetails, appProperties.getAuth().getAccessTokenExpirationMilliseconds(), JwtType.ACCESS_TOKEN);
    }
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(userDetails, appProperties.getAuth().getRefreshTokenExpirationMilliseconds(), JwtType.REFRESH_TOKEN);
    }

    public String generateResetToken(UserDetails userDetails) {
        return buildToken(userDetails, appProperties.getAuth().getResetTokenExpirationMilliseconds(), JwtType.RESET_TOKEN);
    }

    public String generateRefreshTokenFromOld(UserDetails userDetails, String oldRefreshToken) {
        return Jwts
                .builder()
                .setId(((UserPrincipal) userDetails).getId())
                .setExpiration(extractExpiration(oldRefreshToken))
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .claim("type", JwtType.REFRESH_TOKEN)
                .signWith(getSignInKey())
                .compact();
    }

    private String buildToken(UserDetails userDetails, long expiration, JwtType type) {
        return Jwts
                .builder()
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .claim("type", type)
                .signWith(getSignInKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    public JwtType extractTokenType(String token) {
        final Claims claims = extractAllClaims(token);
        return JwtType.valueOf((String) claims.get("type"));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsFunction) {
        final Claims claims = extractAllClaims(token);
        return claimsFunction.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Key getSignInKey() {
        byte[] bytes = Decoders.BASE64.decode(appProperties.getAuth().getTokenSecret());
        return Keys.hmacShaKeyFor(bytes);
    }
}
