package io.github.synepis.todo.todo.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.github.synepis.todo.todo.Todo;
import io.github.synepis.todo.todo.TodoPriority;
import io.github.synepis.todo.todo.TodoStatus;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import javax.validation.constraints.NotNull;
import java.time.Instant;

@Value
@Builder
@JsonDeserialize(builder = TodoDto.TodoDtoBuilder.class)
public class TodoDto {

    @NotNull
    private Long id;

    @NonNull
    private Long userId;

    @NonNull
    private String title;

    private String description;

    @NonNull
    private Instant createdOn;

    @NonNull
    private TodoPriority priority;

    @NonNull
    private TodoStatus status;

    @JsonPOJOBuilder(withPrefix = "")
    public static class TodoDtoBuilder {
    }

    public static TodoDto map(Todo todo) {
        return TodoDto.builder()
                .id(todo.getId())
                .userId(todo.getUserId())
                .title(todo.getTitle())
                .description(todo.getDescription())
                .createdOn(todo.getCreatedOn())
                .priority(todo.getPriority())
                .status(todo.getStatus())
                .build();
    }
}


