package io.github.synepis.todo.user.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.github.synepis.todo.user.User;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
@JsonDeserialize(builder = UserDto.UserDtoBuilder.class)
public class UserDto {

    @NonNull
    private Long id;

    @NonNull
    private String username;

    @NonNull
    private String email;

    @NonNull
    private Instant createdOn;

    private Instant lastLoginOn;

    @JsonPOJOBuilder(withPrefix = "")
    public static class UserDtoBuilder {
    }

    public static UserDto map(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdOn(user.getCreatedOn())
                .lastLoginOn(user.getLastLoginOn())
                .build();
    }
}
