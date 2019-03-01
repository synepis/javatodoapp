package io.github.synepis.todo.login;

import io.github.synepis.todo.exception.ResourceNotFoundException;
import io.github.synepis.todo.login.dto.CreateLoginRequest;
import io.github.synepis.todo.login.dto.UserLoginDto;
import io.github.synepis.todo.security.annotations.AuthorizedAsAdmin;
import io.github.synepis.todo.security.annotations.AuthorizedAsAdminOrOwner;
import io.github.synepis.todo.user.User;
import io.github.synepis.todo.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class UserLoginController {

    private final UserRepository userRepository;

    private final UserLoginRepository userLoginRepository;

    private final Clock clock;

    private final PasswordEncoder passwordEncoder;

    private final long autTokenLifetime;

    public UserLoginController(UserRepository userRepository,
                               UserLoginRepository userLoginRepository,
                               Clock clock,
                               PasswordEncoder passwordEncoder,
                               @Value("${security.token.auth-token-lifetime}") long autTokenLifetime) {
        this.userRepository = userRepository;
        this.userLoginRepository = userLoginRepository;
        this.clock = clock;
        this.passwordEncoder = passwordEncoder;
        this.autTokenLifetime = autTokenLifetime;
    }

    @GetMapping("/logins")
    @AuthorizedAsAdmin
    public List<UserLoginDto> getAllLogins() {
        return userLoginRepository.findAll().stream()
                .map(UserLoginDto::map)
                .collect(Collectors.toList());
    }

    @GetMapping("/users/{userId}/logins")
    @AuthorizedAsAdminOrOwner
    public List<UserLoginDto> getAllLoginsForUser(@PathVariable long userId) {
        return userLoginRepository.findByAllByUserId(userId).stream()
                .map(UserLoginDto::map)
                .collect(Collectors.toList());
    }

    @PostMapping("/logins")
    public UserLoginDto createLogin(@RequestBody @Valid CreateLoginRequest loginRequest) {
        var username = loginRequest.getUsername();
        var password = loginRequest.getPassword();

        var user = userRepository.findByUsername(username);

        if (!user.isPresent() || !validCredentials(user.get(), username, password)) {
            throw new BadCredentialsException("Bad credentials were provided");
        }

        var authToken = UUID.randomUUID().toString();
        var userLogin = userLoginRepository.insert(
                UserLogin.builder()
                        .userId(user.get().getId())
                        .authToken(authToken)
                        .createdOn(clock.instant())
                        .expiresOn(clock.instant().plusSeconds(autTokenLifetime))
                        .build());

        log.info("User {} logged in", user.get());

        return UserLoginDto.map(userLogin);
    }

    @DeleteMapping("/logins/{loginId}")
    @AuthorizedAsAdmin
    public void deleteLogin(@PathVariable long loginId) throws ResourceNotFoundException {
        var userLogin = userLoginRepository.findById(loginId);

        if (!userLogin.isPresent()) {
            throw new ResourceNotFoundException("user login not found");
        }

        userLoginRepository.delete(userLogin.get());
    }


    @DeleteMapping("/users/{userId}/logins")
    @AuthorizedAsAdminOrOwner
    public void deleteAllLoginsForUser(@PathVariable long userId) {
        int tokensDeleted = userLoginRepository.deleteAllForUserId(userId);
        log.info("Logging out user (id={}), deleted {} tokens", userId, tokensDeleted);
    }


    @DeleteMapping("/users/{userId}/logins/{loginId}")
    @AuthorizedAsAdminOrOwner
    public void deleteLoginForUser(@PathVariable long userId, @PathVariable long loginId) throws ResourceNotFoundException {
        var userLogin = userLoginRepository.findByIdAndUserId(loginId, userId);

        if (!userLogin.isPresent()) {
            throw new ResourceNotFoundException("user login not found");
        }

        userLoginRepository.delete(userLogin.get());
    }

    private boolean validCredentials(User user, String username, String password) {
        return user != null
                && user.getUsername().equals(username)
                && passwordEncoder.matches(password, user.getPassword());
    }
}
