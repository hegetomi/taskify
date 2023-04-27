package com.hegetomi.taskify.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class JwtService {
    private static final String ISSUER = "Taskify";
    private static final Algorithm SECRET = Algorithm.HMAC256("myS4lt1S3Cr3t*");

    public String createJwtToken(UserDetails principal) {
        return JWT.create()
                .withSubject(principal.getUsername())
                .withArrayClaim("auth", principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray(String[]::new))
                .withExpiresAt(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(100)))
                .withIssuer(ISSUER)
                .sign(SECRET);
    }

    public UserDetails parseJwt(String jwtToken) {
        DecodedJWT dec = JWT.require(SECRET)
                .withIssuer(ISSUER)
                .build()
                .verify(jwtToken);

        return new User(dec.getSubject(), "", dec.getClaim("auth")
                .asList(String.class)
                .stream()
                .map(SimpleGrantedAuthority::new)
                .toList());
    }

    public boolean isTokenExpired(String jwtToken) {
        return JWT.decode(jwtToken).getExpiresAt().before(new Date());
    }
}
