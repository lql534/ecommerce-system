package com.ecommerce.integration;

import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("用户模块集成测试")
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeAll
    static void setUpAll(@Autowired UserRepository userRepository) {
        // 创建测试用户
        if (userRepository.findByUsername("testuser").isEmpty()) {
            User testUser = new User();
            testUser.setUsername("testuser");
            testUser.setPassword("test123");
            testUser.setEmail("test@example.com");
            testUser.setRole("USER");
            testUser.setStatus(1);
            userRepository.save(testUser);
        }

        if (userRepository.findByUsername("disableduser").isEmpty()) {
            User disabledUser = new User();
            disabledUser.setUsername("disableduser");
            disabledUser.setPassword("disabled123");
            disabledUser.setEmail("disabled@example.com");
            disabledUser.setRole("USER");
            disabledUser.setStatus(0);
            userRepository.save(disabledUser);
        }
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("集成测试 - 用户登录成功")
    void login_Integration_WithValidCredentials_ShouldSucceed() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "testuser");
        loginRequest.put("password", "test123");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("集成测试 - 用户登录失败-密码错误")
    void login_Integration_WithWrongPassword_ShouldFail() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "testuser");
        loginRequest.put("password", "wrongpassword");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("集成测试 - 用户登录失败-用户不存在")
    void login_Integration_WithNonExistentUser_ShouldFail() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "nonexistent");
        loginRequest.put("password", "password");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("集成测试 - 禁用用户登录失败")
    void login_Integration_WithDisabledUser_ShouldFail() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "disableduser");
        loginRequest.put("password", "disabled123");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("集成测试 - 获取用户信息")
    void getUser_Integration_ShouldReturnUserInfo() throws Exception {
        User user = userRepository.findByUsername("testuser").orElseThrow();

        mockMvc.perform(get("/api/users/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }
}
