package io.github.synepis.todo.security;

import io.github.synepis.todo.user.UserRole;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

public class AuthTokenContainer extends AbstractAuthenticationToken {

    private final String token;

    private final Long userId;

    public AuthTokenContainer(String token) {
        super(null);

        this.token = token;
        this.userId = null;
        setAuthenticated(false);
    }

    public AuthTokenContainer(String token, long userId, List<GrantedAuthority> grantedAuthorities) {
        super(grantedAuthorities);
        this.token = token;
        this.userId = userId;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return getToken();
    }

    @Override
    public String getPrincipal() {
        return  getUserId().toString();
    }

    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public boolean hasRole(UserRole userRole) {
        if (this.getAuthorities() == null) {
            return false;
        }

        var roles = this.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return roles.contains(userRole.name());
    }
}
