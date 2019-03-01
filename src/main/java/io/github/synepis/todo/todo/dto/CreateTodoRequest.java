package io.github.synepis.todo.todo.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.github.synepis.todo.todo.TodoPriority;
import io.github.synepis.todo.todo.TodoStatus;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
@Builder
@JsonDeserialize(builder = CreateTodoRequest.CreateTodoRequestBuilder.class)
public class CreateTodoRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private TodoPriority todoPriority;

    @NotNull
    private TodoStatus todoStatus;

    @JsonPOJOBuilder(withPrefix = "")
    public static class CreateTodoRequestBuilder {
    }
}
