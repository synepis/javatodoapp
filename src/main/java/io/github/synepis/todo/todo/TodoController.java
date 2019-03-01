package io.github.synepis.todo.todo;

import io.github.synepis.todo.exception.ResourceNotFoundException;
import io.github.synepis.todo.security.annotations.AuthorizedAsAdmin;
import io.github.synepis.todo.security.annotations.AuthorizedAsAdminOrOwner;
import io.github.synepis.todo.todo.dto.CreateTodoRequest;
import io.github.synepis.todo.todo.dto.TodoDto;
import io.github.synepis.todo.todo.dto.UpdateTodoRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Clock;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.synepis.todo.utils.NullUtils.firstNonNull;

@Slf4j
@RestController
public class TodoController {

    private final Clock clock;

    private final TodoRepository todoRepository;

    public TodoController(Clock clock, TodoRepository todoRepository) {
        this.clock = clock;
        this.todoRepository = todoRepository;
    }

    @GetMapping("/todos")
    @AuthorizedAsAdmin
    public List<TodoDto> getAllTodos() {
        return todoRepository.findAll().stream()
                .map(TodoDto::map)
                .collect(Collectors.toList());
    }

    @GetMapping("/todos/{todoId}")
    @AuthorizedAsAdmin
    public TodoDto getTodo(@PathVariable long todoId) throws ResourceNotFoundException {
        return todoRepository.findById(todoId)
                .map(TodoDto::map)
                .orElseThrow(() -> new ResourceNotFoundException("todo not found"));
    }

    @GetMapping("/users/{userId}/todos")
    @AuthorizedAsAdminOrOwner
    public List<TodoDto> getAllTodosForUser(@PathVariable long userId) {
        return todoRepository.findByUserId(userId).stream()
                .map(TodoDto::map)
                .collect(Collectors.toList());
    }

    @GetMapping("/users/{userId}/todos/{todoId}")
    @AuthorizedAsAdminOrOwner
    public List<TodoDto> getTodoForUser(@PathVariable long todoId, @PathVariable long userId) {
        return todoRepository.findByIdAndUserId(todoId, userId).stream()
                .map(TodoDto::map)
                .collect(Collectors.toList());
    }

    @PostMapping("/users/{userId}/todos")
    @AuthorizedAsAdminOrOwner
    public TodoDto createTodoForUser(@PathVariable long userId,
                                     @Valid @RequestBody CreateTodoRequest createTodoRequest) {
        var todo = todoRepository.insert(
                Todo.builder()
                        .userId(userId)
                        .title(createTodoRequest.getTitle())
                        .description(createTodoRequest.getDescription())
                        .status(createTodoRequest.getTodoStatus())
                        .priority(createTodoRequest.getTodoPriority())
                        .createdOn(clock.instant())
                        .build());
        log.info("Created todo: {}", todo);
        return TodoDto.map(todo);
    }

    @PutMapping("/users/{userId}/todos/{todoId}")
    @AuthorizedAsAdminOrOwner
    public void updateTodoForUser(@PathVariable long userId,
                                  @PathVariable long todoId,
                                  @Valid @RequestBody UpdateTodoRequest updateTodoRequest) throws ResourceNotFoundException {
        var todoOptional = todoRepository.findByIdAndUserId(todoId, userId);
        if (!todoOptional.isPresent()) {
            throw new ResourceNotFoundException("todo not found");
        }

        var todo = todoOptional.get();
        var todoToUpdate =
                Todo.builder()
                        .id(todoId)
                        .userId(userId)
                        .title(firstNonNull(updateTodoRequest.getTitle(), todo.getTitle()))
                        .description(firstNonNull(updateTodoRequest.getDescription(), todo.getDescription()))
                        .status(firstNonNull(updateTodoRequest.getTodoStatus(), todo.getStatus()))
                        .priority(firstNonNull(updateTodoRequest.getTodoPriority(), todo.getPriority()))
                        .createdOn(todo.getCreatedOn())
                        .build();

        todoRepository.update(todoToUpdate);
    }

    @DeleteMapping("/users/{userId}/todos/{todoId}")
    @AuthorizedAsAdminOrOwner
    public void deleteTodoForUser(@PathVariable long userId, @PathVariable long todoId) throws ResourceNotFoundException {
        var userLogin = todoRepository.findByIdAndUserId(todoId, userId);

        if (!userLogin.isPresent()) {
            throw new ResourceNotFoundException("todo not found");
        }

        todoRepository.delete(userLogin.get());
    }
}
