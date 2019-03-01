package io.github.synepis.todo.security;

import io.github.synepis.todo.login.UserLogin;
import io.github.synepis.todo.login.UserLoginRepository;
import io.github.synepis.todo.user.UserRepository;
import io.github.synepis.todo.user.UserRole;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AuthProvider implements AuthenticationProvider {

    private final Clock clock;

    private final UserRepository userRepository;

    private final UserLoginRepository userLoginRepository;

    public AuthProvider(Clock clock, UserRepository userRepository, UserLoginRepository userLoginRepository) {
        this.clock = clock;
        this.userRepository = userRepository;
        this.userLoginRepository = userLoginRepository;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var login = (AuthTokenContainer) authentication;
        var authToken = login.getToken();

        var userLogin = userLoginRepository.findByAuthToken(authToken);

        if (!userLogin.isPresent()) {
            throw new BadCredentialsException("Invalid auth token");
        }

        if (!validUserLogin(userLogin.get())) {
            throw new BadCredentialsException("Expired auth token");
        }

        var user = userRepository.findById(userLogin.get().getUserId());

        if (!user.isPresent()) {
            throw new BadCredentialsException("Unknown user for auth token");
        }

        return new AuthTokenContainer(
                authToken,
                user.get().getId(),
                parseAuthorities(user.get().getRoles()));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return AuthTokenContainer.class.isAssignableFrom(authentication);
    }

    private boolean validUserLogin(UserLogin userLogin) {
        return userLogin.getExpiresOn().isAfter(clock.instant());
    }

    private List<GrantedAuthority> parseAuthorities(Set<UserRole> roles) {
        return roles != null
                ? roles.stream().map(r -> new SimpleGrantedAuthority(r.name())).collect(Collectors.toList())
                : List.of();
    }
}
