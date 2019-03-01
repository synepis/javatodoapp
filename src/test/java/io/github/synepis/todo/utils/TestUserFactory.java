package io.github.synepis.todo.utils;

import io.github.synepis.todo.user.User;
import io.github.synepis.todo.user.UserRepository;
import io.github.synepis.todo.user.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;

@Component
public class TestUserFactory {

    private Long userCounter = 1L;

    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;

    public TestUserFactory(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User aUser() {
        return aUser(Set.of(UserRole.ROLE_USER));
    }

    public User anAdminUser() {
        return aUser(Set.of(UserRole.ROLE_USER, UserRole.ROLE_ADMIN));
    }

    public User aUser(Set<UserRole> roles) {
        var userId = userCounter++;
        var username = "user" + userId;
        var password = username + "password";
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .email("user" + userId + "@mail.com")
                .createdOn(Instant.now())
                .roles(roles)
                .build();

        return userRepository.insert(user);
    }
}
