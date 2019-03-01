package io.github.synepis.todo.user.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.github.synepis.todo.user.UserRole;
import io.github.synepis.todo.validation.ValidEmailOrNull;
import io.github.synepis.todo.validation.ValidPasswordOrNull;
import io.github.synepis.todo.validation.ValidUsernameOrNull;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

@Value
@Builder
@JsonDeserialize(builder = UpdateUserRequest.UpdateUserRequestBuilder.class)
public class UpdateUserRequest {

    @ValidUsernameOrNull
    private String username;

    @ValidPasswordOrNull
    private String password;

    @ValidEmailOrNull
    private String email;

    private Set<UserRole> roles;

    @JsonPOJOBuilder(withPrefix = "")
    public static class UpdateUserRequestBuilder {
    }
}
