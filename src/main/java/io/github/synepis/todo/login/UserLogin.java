package io.github.synepis.todo.login;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Wither;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.Instant;

@Value
@Builder
@Wither
public class UserLogin {

    private long id;

    private long userId;

    @NonNull
    private String authToken;

    @NonNull
    private Instant createdOn;

    @NonNull
    private Instant expiresOn;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("userId", userId)
                .append("authToken", authToken)
                .append("createdOn", createdOn)
                .append("expiresOn", expiresOn)
                .build();
    }
}
