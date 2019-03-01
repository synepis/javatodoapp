package io.github.synepis.todo.todo.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.github.synepis.todo.todo.TodoPriority;
import io.github.synepis.todo.todo.TodoStatus;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonDeserialize(builder = UpdateTodoRequest.UpdateTodoRequestBuilder.class)
public class UpdateTodoRequest {

    private String title;

    private String description;

    private TodoPriority todoPriority;

    private TodoStatus todoStatus;

    @JsonPOJOBuilder(withPrefix = "")
    public static class UpdateTodoRequestBuilder {
    }
}

