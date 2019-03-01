package io.github.synepis.todo.utils;

import io.github.synepis.todo.todo.Todo;
import io.github.synepis.todo.todo.TodoPriority;
import io.github.synepis.todo.todo.TodoRepository;
import io.github.synepis.todo.todo.TodoStatus;
import io.github.synepis.todo.user.User;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TestTodoFactory {

    private Long todoCounter = 1L;

    private TodoRepository todoRepository;

    public TestTodoFactory(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public Todo aTodo(User user) {
        var todoId = todoCounter++;
        var todo = Todo.builder()
                .userId(user.getId())
                .title("Todo" + todoId)
                .description("Description" + todoId)
                .createdOn(Instant.now())
                .status(TodoStatus.NOT_STARTED)
                .priority(TodoPriority.MEDIUM)
                .build();

        return todoRepository.insert(todo);
    }

}
