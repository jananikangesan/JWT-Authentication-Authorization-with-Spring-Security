package com.example.demo.service;


import com.example.demo.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final String SECRET_KEY="be347d644eddf843fd6cc27298e1214326db69cfb7669601365434e3a24233ca";

  public String extractUsername(String token){
    return extractClaim(token,Claims::getSubject);
  }

  public boolean isValid(String token, UserDetails user){
    String username=extractUsername(token);
    return (username.equals(user.getUsername())) && !isTokenExpired(token);
  }

  private boolean isTokenExpired(String token){
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    return extractClaim(token,Claims::getExpiration);
  }

  public <T> T extractClaim(String token, Function<Claims,T> resolver){
    Claims claims=extractALLClaims(token);
    return resolver.apply(claims);
  }

  private Claims extractALLClaims(String token){
    return Jwts.parser()
        .verifyWith(getSigninKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public String generateToken(User user){
    String token= Jwts.builder()
        .subject(user.getUsername())
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis()+24*60*60*1000))
        .signWith(getSigninKey())
        .compact();

    return token;
  }

  private SecretKey getSigninKey(){
    byte[] keyBytes= Decoders.BASE64URL.decode(SECRET_KEY);

    return Keys.hmacShaKeyFor(keyBytes);
  }
}
