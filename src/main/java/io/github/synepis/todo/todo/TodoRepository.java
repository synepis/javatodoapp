package io.github.synepis.todo.todo;

import io.github.synepis.todo.utils.SqlUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Repository
public class TodoRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public TodoRepository(NamedParameterJdbcTemplate jdbcTemplate) {this.jdbcTemplate = jdbcTemplate;}

    public Long count() {
        return jdbcTemplate.query(
                "select count(*) from " + TABLE_NAME, Map.of(), SqlUtils::mapCountRow)
                .stream().findFirst().get();
    }

    public Optional<Todo> findById(Long id) {
        return jdbcTemplate.query(
                "select * from " + TABLE_NAME + " where " + FIELD_ID + " = :" + FIELD_ID,
                Map.of(FIELD_ID, id),
                this::mapRow).stream().findFirst();
    }

    public Optional<Todo> findByIdAndUserId(long id, long userId) {
        return jdbcTemplate.query(
                "select * from " + TABLE_NAME + " where " +
                        FIELD_ID + " = :" + FIELD_ID + " and " +
                        FIELD_USER_ID + " = :" + FIELD_USER_ID,
                Map.of(FIELD_ID, id, FIELD_USER_ID, userId),
                this::mapRow).stream().findFirst();
    }

    public List<Todo> findAll() {
        return jdbcTemplate.query("select * from " + TABLE_NAME, Map.of(), this::mapRow);
    }

    public List<Todo> findByUserId(Long userId) {
        return jdbcTemplate.query(
                "select * from " + TABLE_NAME + " where " + FIELD_USER_ID + " = :" + FIELD_USER_ID ,
                Map.of(FIELD_USER_ID, userId),
                this::mapRow);
    }

    public Todo insert(Todo todo) {
        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                "insert into " + TABLE_NAME + "(" +
                        FIELD_USER_ID + ", " +
                        FIELD_TITLE + ", " +
                        FIELD_DESCRIPTION + ", " +
                        FIELD_CREATED_ON + ", " +
                        FIELD_PRIORITY + ", " +
                        FIELD_STATUS + ")" +
                        " values (" +
                        ":" + FIELD_USER_ID + ", " +
                        ":" + FIELD_TITLE + ", " +
                        ":" + FIELD_DESCRIPTION + ", " +
                        ":" + FIELD_CREATED_ON + ", " +
                        ":" + FIELD_PRIORITY + ", " +
                        ":" + FIELD_STATUS + ")",
                SqlUtils.parameterMap(
                        FIELD_USER_ID, todo.getUserId(),
                        FIELD_TITLE, todo.getTitle(),
                        FIELD_DESCRIPTION, todo.getDescription(),
                        FIELD_CREATED_ON, Timestamp.from(todo.getCreatedOn()),
                        FIELD_PRIORITY, todo.getPriority().name(),
                        FIELD_STATUS, todo.getStatus().name()),
                keyHolder);
        return todo.withId((Long) Objects.requireNonNull(keyHolder.getKeys()).get(FIELD_ID));
    }

    public boolean update(Todo todo) {
        var updateCount = jdbcTemplate.update(
                "update " + TABLE_NAME + " set " +
                        FIELD_USER_ID + " = :" + FIELD_USER_ID + ", " +
                        FIELD_TITLE + " = :" + FIELD_TITLE + ", " +
                        FIELD_DESCRIPTION + " = :" + FIELD_DESCRIPTION + ", " +
                        FIELD_CREATED_ON + " = :" + FIELD_CREATED_ON + ", " +
                        FIELD_PRIORITY + " = :" + FIELD_PRIORITY + ", " +
                        FIELD_STATUS + " = :" + FIELD_STATUS +
                        " where " +
                        FIELD_ID + " = :" + FIELD_ID,
                SqlUtils.parameterMap(
                        FIELD_ID, todo.getId(),
                        FIELD_USER_ID,  todo.getUserId(),
                        FIELD_TITLE,  todo.getTitle(),
                        FIELD_DESCRIPTION, todo.getDescription(),
                        FIELD_CREATED_ON, Timestamp.from(todo.getCreatedOn()),
                        FIELD_PRIORITY, todo.getPriority().name(),
                        FIELD_STATUS, todo.getStatus().name()));
        return updateCount == 1;
    }

    public boolean delete(Todo todo) {
        var updateCount = jdbcTemplate.update(
                "delete from " + TABLE_NAME + " where " + FIELD_ID + " = :" + FIELD_ID,
                Map.of(FIELD_ID, todo.getId()));
        return updateCount == 1;
    }

    private final String TABLE_NAME = "todo";

    private final String FIELD_ID = "id";
    private final String FIELD_USER_ID = "user_id";
    private final String FIELD_TITLE = "title";
    private final String FIELD_DESCRIPTION = "description";
    private final String FIELD_CREATED_ON = "created_on";
    private final String FIELD_PRIORITY = "priority";
    private final String FIELD_STATUS = "status";

    private Todo mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Todo.builder()
                .id(rs.getLong(FIELD_ID))
                .userId(rs.getLong(FIELD_USER_ID))
                .title(rs.getString(FIELD_TITLE))
                .description(rs.getString(FIELD_DESCRIPTION))
                .createdOn(rs.getTimestamp(FIELD_CREATED_ON).toInstant())
                .priority(TodoPriority.valueOf(rs.getString(FIELD_PRIORITY)))
                .status(TodoStatus.valueOf(rs.getString(FIELD_STATUS)))
                .build();
    }
}
