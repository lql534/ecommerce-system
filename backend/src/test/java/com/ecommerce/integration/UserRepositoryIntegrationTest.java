package com.ecommerce.integration;

import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("用户Repository集成测试")
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setRole("USER");
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("保存用户 - 应成功保存并生成ID")
    void save_ShouldPersistUser() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("newpass123");
        newUser.setEmail("new@example.com");
        newUser.setRole("USER");

        User saved = userRepository.save(newUser);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("newuser");
    }

    @Test
    @DisplayName("根据用户名查询 - 存在时应返回用户")
    void findByUsername_WhenExists_ShouldReturnUser() {
        Optional<User> found = userRepository.findByUsername("testuser");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("根据用户名查询 - 不存在时应返回空")
    void findByUsername_WhenNotExists_ShouldReturnEmpty() {
        Optional<User> found = userRepository.findByUsername("nonexistent");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("根据邮箱查询 - 应返回对应用户")
    void findByEmail_ShouldReturnUser() {
        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("检查用户名是否存在 - 存在时返回true")
    void existsByUsername_WhenExists_ShouldReturnTrue() {
        boolean exists = userRepository.existsByUsername("testuser");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("检查用户名是否存在 - 不存在时返回false")
    void existsByUsername_WhenNotExists_ShouldReturnFalse() {
        boolean exists = userRepository.existsByUsername("nonexistent");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("检查邮箱是否存在 - 应正确判断")
    void existsByEmail_ShouldReturnCorrectResult() {
        assertThat(userRepository.existsByEmail("test@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("notexist@example.com")).isFalse();
    }

    @Test
    @DisplayName("更新用户信息 - 应成功更新")
    void update_ShouldModifyUser() {
        testUser.setEmail("updated@example.com");
        testUser.setPhone("13900139000");
        userRepository.save(testUser);

        User updated = userRepository.findById(testUser.getId()).orElseThrow();

        assertThat(updated.getEmail()).isEqualTo("updated@example.com");
        assertThat(updated.getPhone()).isEqualTo("13900139000");
    }

    @Test
    @DisplayName("删除用户 - 应成功删除")
    void delete_ShouldRemoveUser() {
        Long id = testUser.getId();
        userRepository.deleteById(id);

        Optional<User> deleted = userRepository.findById(id);

        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("管理员角色用户 - 应正确保存角色")
    void save_AdminUser_ShouldPersistRole() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("admin123");
        admin.setEmail("admin@example.com");
        admin.setRole("ADMIN");

        User saved = userRepository.save(admin);

        assertThat(saved.getRole()).isEqualTo("ADMIN");
    }
}
