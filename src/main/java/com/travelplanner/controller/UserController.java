package com.travelplanner.controller;

import com.travelplanner.model.User;
import com.travelplanner.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Collection;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {
    
    @Autowired
    private UserService userService;
    
    // 用户注册
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User userInfo, HttpSession session) {
        try {
            User user = userService.registerUser(userInfo.getUsername(), userInfo.getPassword());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("用户名已存在"));
            }
            // 注册成功后将用户名存储在会话中
            session.setAttribute("username", user.getUsername());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("注册失败: " + e.getMessage()));
        }
    }
    
    // 用户登录
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User credentials, HttpSession session) {
        User user = userService.authenticateUser(credentials.getUsername(), credentials.getPassword());
        if (user != null) {
            // 登录成功后将用户名存储在会话中
            session.setAttribute("username", user.getUsername());
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("用户名或密码错误"));
        }
    }
    
    // 检查用户会话状态
    @GetMapping("/check-status")
    public ResponseEntity<?> checkStatus(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username != null) {
            User user = userService.getUserByUsername(username);
            if (user != null) {
                return ResponseEntity.ok(user);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("用户未登录"));
    }
    
    // 获取所有用户（仅用于调试）
    @GetMapping("/all")
    public Collection<User> getAllUsers() {
        return userService.getAllUsers();
    }
    
    // 获取用户信息
    @GetMapping("/{id}")
    public User getUserInfo(@PathVariable Long id) {
        return userService.getUser(id);
    }
    
    // 更新用户信息
    @PutMapping("/{id}")
    public User updateUserInfo(@PathVariable Long id, @RequestBody User updates) {
        return userService.updateUser(id, updates);
    }
    
    // 删除用户
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return "User " + id + " deleted successfully";
        } else {
            return "Failed to delete user " + id;
        }
    }
    
    // 错误响应类
    private static class ErrorResponse {
        private String message;
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
}