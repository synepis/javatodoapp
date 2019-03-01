package io.github.synepis.todo.utils;

import io.github.synepis.todo.security.AuthTokenContainer;
import io.github.synepis.todo.user.UserRole;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static boolean currentlyAuthenticatedOrIsAdmin(long userId) {
        var authTokenContainer = (AuthTokenContainer)SecurityContextHolder.getContext().getAuthentication();
        if (authTokenContainer == null) {
            return false;
        }
        return authTokenContainer.getUserId() == userId || authTokenContainer.hasRole(UserRole.ROLE_ADMIN);
    }
}
