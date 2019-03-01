package io.github.synepis.todo.user;

import io.github.synepis.todo.utils.SqlUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class UserRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UserRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long count() {
        return jdbcTemplate.query(
                "select count(*) from " + TABLE_NAME, Map.of(), SqlUtils::mapCountRow)
                .stream().findFirst().get();
    }

    public Optional<User> findById(Long id) {
        return jdbcTemplate.query(
                "select * from " + TABLE_NAME + " where " + FIELD_ID + " = :" + FIELD_ID,
                Map.of(FIELD_ID, id),
                this::mapRow).stream().findFirst();
    }

    public Optional<User> findByUsername(String username) {
        return jdbcTemplate.query(
                "select * from " + TABLE_NAME + " where " + FIELD_USERNAME + " = :" + FIELD_USERNAME,
                Map.of(FIELD_USERNAME, username),
                this::mapRow).stream().findFirst();
    }

    public List<User> findAll() {
        return jdbcTemplate.query("select * from " + TABLE_NAME, Map.of(), this::mapRow);
    }

    public User insert(User user) {
        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                "insert into " + TABLE_NAME + "(" +
                        FIELD_USERNAME + "," +
                        FIELD_EMAIL + "," +
                        FIELD_PASSWORD + "," +
                        FIELD_CREATED_ON + "," +
                        FIELD_LAST_LOGIN_ON + "," +
                        FIELD_ROLES + ")" +
                        " values (" +
                        ":" + FIELD_USERNAME + "," +
                        ":" + FIELD_EMAIL + "," +
                        ":" + FIELD_PASSWORD + "," +
                        ":" + FIELD_CREATED_ON + "," +
                        ":" + FIELD_LAST_LOGIN_ON + "," +
                        ":" + FIELD_ROLES + ")",
                SqlUtils.parameterMap(
                        FIELD_USERNAME, user.getUsername(),
                        FIELD_EMAIL, user.getEmail(),
                        FIELD_PASSWORD, user.getPassword(),
                        FIELD_CREATED_ON, SqlUtils.mapNullableInstant(user.getCreatedOn()),
                        FIELD_LAST_LOGIN_ON, SqlUtils.mapNullableInstant(user.getLastLoginOn()),
                        FIELD_ROLES, mapRoles(user.getRoles())),
                keyHolder);
        return user.withId((Long) Objects.requireNonNull(keyHolder.getKeys()).get(FIELD_ID));
    }

    public boolean update(User user) {
        var updateCount = jdbcTemplate.update(
                "update " + TABLE_NAME + " set " +
                        FIELD_USERNAME + " = :" + FIELD_USERNAME + "," +
                        FIELD_EMAIL + " = :" + FIELD_EMAIL + "," +
                        FIELD_PASSWORD + " = :" + FIELD_PASSWORD + "," +
                        FIELD_CREATED_ON + " = :" + FIELD_CREATED_ON + "," +
                        FIELD_LAST_LOGIN_ON + " = :" + FIELD_LAST_LOGIN_ON + "," +
                        FIELD_ROLES + " = :" + FIELD_ROLES +
                        " where " +
                        FIELD_ID + " = :" + FIELD_ID,
                SqlUtils.parameterMap(
                        FIELD_ID, user.getId(),
                        FIELD_USERNAME, user.getUsername(),
                        FIELD_PASSWORD, user.getPassword(),
                        FIELD_EMAIL, user.getEmail(),
                        FIELD_CREATED_ON, Timestamp.from(user.getCreatedOn()),
                        FIELD_LAST_LOGIN_ON, SqlUtils.mapNullableInstant(user.getLastLoginOn()),
                        FIELD_ROLES, mapRoles(user.getRoles())));
        return updateCount == 1;
    }

    public boolean delete(User user) {
        var updateCount = jdbcTemplate.update(
                "delete from " + TABLE_NAME + " where " + FIELD_ID + " = :" + FIELD_ID,
                Map.of(FIELD_ID, user.getId()));
        return updateCount == 1;
    }

    private final String TABLE_NAME = "public.\"user\"";

    private final String FIELD_ID = "id";
    private final String FIELD_USERNAME = "username";
    private final String FIELD_EMAIL = "email";
    private final String FIELD_PASSWORD = "password";
    private final String FIELD_CREATED_ON = "created_on";
    private final String FIELD_LAST_LOGIN_ON = "last_login_on";
    private final String FIELD_ROLES = "roles";

    private User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(rs.getLong(FIELD_ID))
                .username(rs.getString(FIELD_USERNAME))
                .email(rs.getString(FIELD_EMAIL))
                .password(rs.getString(FIELD_PASSWORD))
                .createdOn(rs.getTimestamp(FIELD_CREATED_ON).toInstant())
                .lastLoginOn(SqlUtils.mapNullableTimestamp(rs.getTimestamp(FIELD_LAST_LOGIN_ON)))
                .roles(parseRoles(rs.getString(FIELD_ROLES)))
                .build();
    }

    private String mapRoles(Set<UserRole> roles) {
        return String.join(",", roles.stream().map(Enum::name).collect(Collectors.toSet()));
    }

    private Set<UserRole> parseRoles(String roles) {
        return roles != null
                ? Stream.of(roles.split(",")).map(UserRole::valueOf).collect(Collectors.toSet())
                : Set.of();
    }
}

