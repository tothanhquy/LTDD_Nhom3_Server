package Nhom3.Server.service;


import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.*;

public class JWTService {
    private static String SECRET_KEY = "tjgL0vk4tkAvnj+IJkM86Ku/nnhtsIF6Q/WpGFkClvU=";
    public static String register(AccountService.JWTContent jwtContent) {
        Gson gson = new Gson();
        String jwtContentString = gson.toJson(jwtContent);
//        System.out.println(jwtContentString);

        Date expiration = new Date(System.currentTimeMillis() + 1000L*60*60*24*30*6); // 6 month
//        System.out.println(expiration);
//        System.out.println(System.currentTimeMillis());
//        System.out.println(System.currentTimeMillis()+ 1000L*60*60*24*30*6);
        // Create JWT
        String jwt = Jwts.builder()
                .setSubject(jwtContentString)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();

//        System.out.println("Generated JWT: " + jwt);
        return jwt;
    }
    public static AccountService.JWTContent parse(String jwt){
        // Parse and verify JWT
        try {
            Jws<Claims> parsedJwt = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(jwt);
            Claims claims = parsedJwt.getBody();

            Gson gson = new Gson();
            AccountService.JWTContent jwtContent = gson.fromJson(claims.getSubject(),AccountService.JWTContent.class);
            if(claims.getExpiration().before(new Date(System.currentTimeMillis()))){
                //over Expiration
                System.out.println(1);
                return null;
            }

            return jwtContent;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
}
