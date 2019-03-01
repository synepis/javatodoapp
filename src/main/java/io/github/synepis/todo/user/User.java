package io.github.synepis.todo.user;

import lombok.*;
import lombok.experimental.Wither;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.Instant;
import java.util.Set;

@Value
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@Wither
public class User {

    private Long id;

    @NonNull
    private String username;

    @NonNull
    private String password;

    @NonNull
    private String email;

    @NonNull
    private Instant createdOn;

    private Instant lastLoginOn;

    @NonNull
    private Set<UserRole> roles;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("username", username)
                .append("email", email)
                .append("createdOn", createdOn)
                .append("lastLoginOn", lastLoginOn)
                .append("roles", roles)
                .build();
    }

}
