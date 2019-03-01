package io.github.synepis.todo.security;

import io.github.synepis.todo.user.UserRole;
import io.github.synepis.todo.user.dto.UpdateUserRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("authorizationService")
public class AuthorizationService {

    public boolean isAdmin() {
        return isAdmin(getAuthentication());
    }

    public boolean isOwner(long userId) {
        return isOwner(getAuthentication(), userId);
    }

    public boolean isAdminOrOwner(long userId) {
        var authentication = getAuthentication();
        return isAdmin(authentication) || isOwner(authentication, userId);
    }

    public boolean isAuthorized(long userId, UpdateUserRequest updateUserRequest) {
        var authentication = getAuthentication();
        var isAdmin = isAdmin(authentication);
        var isOwner = isOwner(authentication, userId);
        var isUpdatingRoles = updateUserRequest.getRoles() != null;
        return isAdmin || (isOwner && !isUpdatingRoles);
    }

    private boolean isAdmin(AuthTokenContainer authentication) {
        return authentication != null && authentication.hasRole(UserRole.ROLE_ADMIN);
    }

    private boolean isOwner(AuthTokenContainer authentication, long userId) {
        return authentication != null && authentication.getUserId() == userId;
    }

    private AuthTokenContainer getAuthentication() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (AuthTokenContainer.class.isAssignableFrom(authentication.getClass())) {
            return (AuthTokenContainer) authentication;
        } else {
            return null;
        }
    }

}
