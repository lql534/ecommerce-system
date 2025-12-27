package com.ecommerce.service;

import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务单元测试")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("admin");
        testUser.setPassword("admin123");
        testUser.setEmail("admin@test.com");
        testUser.setRole("ADMIN");
        testUser.setStatus(1);
    }

    @Test
    @DisplayName("登录成功 - 用户名密码正确")
    void login_WithValidCredentials_ShouldReturnUser() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.login("admin", "admin123");

        assertTrue(result.isPresent());
        assertEquals("admin", result.get().getUsername());
        verify(userRepository, times(1)).findByUsername("admin");
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void login_WithWrongPassword_ShouldReturnEmpty() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.login("admin", "wrongpassword");

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("登录失败 - 用户不存在")
    void login_WithNonExistentUser_ShouldReturnEmpty() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Optional<User> result = userService.login("nonexistent", "password");

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("登录失败 - 用户被禁用")
    void login_WithDisabledUser_ShouldReturnEmpty() {
        testUser.setStatus(0);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.login("admin", "admin123");

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("根据ID获取用户 - 存在")
    void getUserById_WhenExists_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals("admin", result.get().getUsername());
    }

    @Test
    @DisplayName("根据ID获取用户 - 不存在")
    void getUserById_WhenNotExists_ShouldReturnEmpty() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("根据用户名获取用户")
    void getUserByUsername_ShouldReturnUser() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.getUserByUsername("admin");

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }
}
