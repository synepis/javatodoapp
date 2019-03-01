package io.github.synepis.todo.user;

import io.github.synepis.todo.exception.ResourceNotFoundException;
import io.github.synepis.todo.security.annotations.AuthorizedAsAdmin;
import io.github.synepis.todo.security.annotations.AuthorizedAsAdminOrOwner;
import io.github.synepis.todo.user.dto.CreateUserRequest;
import io.github.synepis.todo.user.dto.UpdateUserRequest;
import io.github.synepis.todo.user.dto.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Clock;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.synepis.todo.utils.NullUtils.firstNonNull;

@Slf4j
@RestController
public class UserController {

    private Clock clock;

    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;

    public UserController(Clock clock, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.clock = clock;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/users")
    @AuthorizedAsAdmin
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDto::map)
                .collect(Collectors.toList());
    }


    @GetMapping("/users/{userId}")
    @AuthorizedAsAdminOrOwner
    public UserDto getUser(@PathVariable("userId") Long userId) throws Exception {
        return userRepository.findById(userId)
                .map(UserDto::map)
                .orElseThrow(() -> new ResourceNotFoundException("user not found"));
    }

    @PostMapping("/users")
    public UserDto createUser(@Valid @RequestBody CreateUserRequest createUserRequest) {
        var user = userRepository.insert(
                User.builder()
                        .username(createUserRequest.getUsername())
                        .email(createUserRequest.getEmail())
                        .password(passwordEncoder.encode(createUserRequest.getPassword()))
                        .createdOn(clock.instant())
                        .roles(Set.of(UserRole.ROLE_USER))
                        .build());
        log.info("Created user: {}", user);
        return UserDto.map(user);
    }

    @PutMapping("/users/{userId}")
    @PreAuthorize("@authorizationService.isAuthorized(#userId, #updateUserRequest)")
    public void updateUser(@PathVariable long userId,
                           @Valid @RequestBody UpdateUserRequest updateUserRequest) throws ResourceNotFoundException {
        var userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new ResourceNotFoundException("user not found");
        }

        var user = userOptional.get();
        var updateUserRequestPassword = updateUserRequest.getPassword() != null
                ? passwordEncoder.encode(updateUserRequest.getPassword())
                : null;
        var userToUpdate =
                User.builder()
                        .id(user.getId())
                        .username(firstNonNull(updateUserRequest.getUsername(), user.getUsername()))
                        .password(firstNonNull(updateUserRequestPassword, user.getPassword()))
                        .email(firstNonNull(updateUserRequest.getEmail(), user.getEmail()))
                        .roles(firstNonNull(updateUserRequest.getRoles(), user.getRoles()))
                        .createdOn(user.getCreatedOn())
                        .lastLoginOn(user.getLastLoginOn())
                        .build();

        userRepository.update(userToUpdate);

        log.info("Updated user {}", user);
    }

    @DeleteMapping("/users/{userId}")
    @AuthorizedAsAdmin
    public void deleteUser(@PathVariable long userId) throws ResourceNotFoundException {
        var userLogin = userRepository.findById(userId);

        if (!userLogin.isPresent()) {
            throw new ResourceNotFoundException("user login not found");
        }

        userRepository.delete(userLogin.get());
    }

}
