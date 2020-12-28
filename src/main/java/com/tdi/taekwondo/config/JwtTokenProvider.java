package com.tdi.taekwondo.config;

import java.util.Base64;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.tdi.taekwondo.service.AdministradorService;
import com.tdi.taekwondo.service.AlumnoService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenProvider {

    @Value("${security.jwt.token.secret-key:secret}")
    private String secretKey = "pendiente_de_exportar_archivo_externo_algun_dia";

    @Value("${security.jwt.token.expire-length:6000000}")
    private long validityInMilliseconds = 6000000; // 10 [minutos] -> (60 [secs]) (10 [min]) (1000 [ms]) = 600000

    @Autowired
    private AlumnoService userDetailsService;
    private AdministradorService adminDetailsService;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createToken(String username) {
        Claims claims = Jwts.claims().setSubject(username);
        Date now = new Date();
        System.out.println("////(&/%sss&");
        System.out.println(username);
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }

    public Authentication getAuthentication(String token) {
    	System.out.println("#@@@@@@@@@@@");
    	System.out.println(token);
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(getUsername(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
    
    public Authentication getAuthenticationAdmin(String token) {
        System.out.println(token);
        System.out.println("//////////////////");
        UserDetails userDetails = this.adminDetailsService.loadUserByUsername(getUsername(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getUsername(String token) {
    	System.out.println("////(&/%&");
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }

    public String resolveToken(HttpServletRequest req) {
    	System.out.println("////(&/%&");
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }

    public boolean validateToken(String token) {
    		System.out.println("////(&/%&");
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            if (claims.getBody().getExpiration().before(new Date())) {
                return false;
            }
            return true;
    }
}
