package com.wang.mycampus.Utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JWT {

    // 密钥长度必须至少满足所选算法的要求（HS256 至少需要 32 字节）
    // 生产环境务必通过环境变量 JWT_SECRET 注入自定义密钥；下方默认值仅用于本地开发，切勿在生产使用
    private static final String SECRET_STRING = System.getenv().getOrDefault(
            "JWT_SECRET", "campus-wall-secret-key-2024-keep-it-very-very-long-and-secure");
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRATION = 86400000; // 24小时

    // 生成 Token
    public static String generateToken(Long userId, String username) {
        return Jwts.builder()
                .subject(userId.toString()) // 设置主体
                .claim("username", username) // 自定义 Claim
                .issuedAt(new Date()) // 签发时间
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION)) // 过期时间
                .signWith(KEY) // 自动推断算法，签名
                .compact();
    }

    // 解析 Token
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY) // 设置签名验证密钥
                .build()         // 构建解析器
                .parseSignedClaims(token) // 解析 JWS
                .getPayload();   // 获取载荷
    }

    // 获取用户 ID
    public static Long getUserId(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    // 判断是否合法（通常解析成功即代表签名正确且未过期）
    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}