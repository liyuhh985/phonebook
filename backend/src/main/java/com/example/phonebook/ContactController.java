package com.example.phonebook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.concurrent.TimeUnit;
import java.sql.*;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ContactController {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    private final String dbHost = System.getenv().getOrDefault("MYSQL_HOST", "localhost");
    private final String dbPort = System.getenv().getOrDefault("MYSQL_PORT", "3306");
    private final String dbUser = System.getenv().getOrDefault("MYSQL_USER", "root");
    private final String dbPassword = System.getenv().getOrDefault("MYSQL_PASSWORD", "root");
    private final String redisHost = System.getenv().getOrDefault("REDIS_HOST", "localhost");
    
    @GetMapping("/search")
    public Result search(@RequestParam String name) {
        // 先查 Redis 缓存
        String cacheKey = "contact:" + name;
        String cachedPhone = redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedPhone != null) {
            Contact contact = new Contact();
            contact.setName(name);
            contact.setPhone(cachedPhone);
            return Result.success(contact);
        }
        
        // 缓存没有，查数据库
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://" + dbHost + ":" + dbPort + "/phonebook?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=utf-8",
                dbUser, dbPassword)) {
            
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM contacts WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                Contact contact = new Contact();
                contact.setId(rs.getInt("id"));
                contact.setName(rs.getString("name"));
                contact.setPhone(rs.getString("phone"));
                
                // 存入 Redis 缓存，5分钟过期
                redisTemplate.opsForValue().set(cacheKey, contact.getPhone(), 5, TimeUnit.MINUTES);
                
                return Result.success(contact);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return Result.fail("未找到联系人");
    }
}

class Result {
    private boolean success;
    private String message;
    private Object data;
    
    public static Result success(Object data) {
        Result r = new Result();
        r.success = true;
        r.data = data;
        return r;
    }
    
    public static Result fail(String message) {
        Result r = new Result();
        r.success = false;
        r.message = message;
        return r;
    }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}
