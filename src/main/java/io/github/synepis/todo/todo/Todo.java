package io.github.synepis.todo.todo;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.Wither;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.Instant;

@Data
@Builder
@EqualsAndHashCode
@Wither
public class Todo {

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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("userId", userId)
                .append("title", title)
                .append("description", description)
                .append("createdOn", createdOn)
                .append("priority", priority)
                .append("status", status)
                .build();
    }
}


