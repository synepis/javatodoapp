package io.github.synepis.todo.exception;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@JsonDeserialize(builder = ApiError.ApiErrorBuilder.class)
public class ApiError {
    private final String status;
    private final List<String> userMessages;
    private final List<String> debugMessages;
    private final Instant createdOn;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ApiErrorBuilder {
    }
}

