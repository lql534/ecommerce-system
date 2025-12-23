package com.ecommerce.controller;

import com.ecommerce.entity.User;
import com.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户登录接口")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录验证")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        return userService.login(username, password)
                .map(user -> {
                    user.setPassword(null); // 不返回密码
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.status(401).body(null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户信息", description = "根据ID获取用户信息")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> {
                    user.setPassword(null);
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
