package io.github.synepis.todo.login;

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
public class UserLoginRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UserLoginRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long count() {
        return jdbcTemplate.query(
                "select count(*) from " + TABLE_NAME, Map.of(), SqlUtils::mapCountRow)
                .stream().findFirst().get();
    }

    public Long countByUserId(long userId) {
        return jdbcTemplate.query(
                "select * from " + TABLE_NAME + " where " + FIELD_USER_ID + " = :" + FIELD_USER_ID,
                Map.of(FIELD_USER_ID, userId),
                SqlUtils::mapCountRow).stream().findFirst().get();
    }

    public List<UserLogin> findAll() {
        return jdbcTemplate.query("select * from " + TABLE_NAME, Map.of(), this::mapRow);
    }

    public Optional<UserLogin> findById(long id) {
        return jdbcTemplate.query(
                "select * from " + TABLE_NAME + " where " + FIELD_ID + " = :" + FIELD_ID,
                Map.of(FIELD_ID, id),
                this::mapRow).stream().findFirst();
    }

    public Optional<UserLogin> findByIdAndUserId(long id, long userId) {
        return jdbcTemplate.query(
                "select * from " + TABLE_NAME + " where " +
                        FIELD_ID + " = :" + FIELD_ID + " and " +
                        FIELD_USER_ID + " = :" + FIELD_USER_ID,
                Map.of(FIELD_ID, id, FIELD_USER_ID, userId),
                this::mapRow).stream().findFirst();
    }

    public List<UserLogin> findByAllByUserId(long userId) {
        return jdbcTemplate.query(
                "select * from " + TABLE_NAME + " where " + FIELD_USER_ID + " = :" + FIELD_USER_ID,
                Map.of(FIELD_USER_ID, userId),
                this::mapRow);
    }

    public Optional<UserLogin> findByAuthToken(String authToken) {
        return jdbcTemplate.query(
                "select * from " + TABLE_NAME + " where " + FIELD_AUTH_TOKEN + " = :" + FIELD_AUTH_TOKEN,
                Map.of(FIELD_AUTH_TOKEN, authToken),
                this::mapRow).stream().findFirst();
    }

    public UserLogin insert(UserLogin userLogin) {
        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                "insert into " + TABLE_NAME + "(" +
                        FIELD_USER_ID + ", " +
                        FIELD_AUTH_TOKEN + ", " +
                        FIELD_CREATED_ON + ", " +
                        FIELD_EXPIRES_ON + ") " +
                        " values (" +
                        ":" + FIELD_USER_ID + ", " +
                        ":" + FIELD_AUTH_TOKEN + ", " +
                        ":" + FIELD_CREATED_ON + ", " +
                        ":" + FIELD_EXPIRES_ON + ")",
                SqlUtils.parameterMap(
                        FIELD_USER_ID, userLogin.getUserId(),
                        FIELD_AUTH_TOKEN, userLogin.getAuthToken(),
                        FIELD_CREATED_ON, Timestamp.from(userLogin.getCreatedOn()),
                        FIELD_EXPIRES_ON, Timestamp.from(userLogin.getExpiresOn())),
                keyHolder);
        return userLogin.withId((Long) Objects.requireNonNull(keyHolder.getKeys()).get(FIELD_ID));
    }

    public boolean update(UserLogin userLogin) {
        var updateCount = jdbcTemplate.update(
                "update " + TABLE_NAME + " set " +
                        FIELD_USER_ID + " = :" + FIELD_USER_ID + ", " +
                        FIELD_AUTH_TOKEN + " = :" + FIELD_AUTH_TOKEN + ", " +
                        FIELD_CREATED_ON + " = :" + FIELD_CREATED_ON + ", " +
                        FIELD_EXPIRES_ON + " = :" + FIELD_EXPIRES_ON +
                        " where " +
                        FIELD_AUTH_TOKEN + " = :" + FIELD_AUTH_TOKEN,
                SqlUtils.parameterMap(
                        FIELD_USER_ID, userLogin.getUserId(),
                        FIELD_AUTH_TOKEN, userLogin.getAuthToken(),
                        FIELD_CREATED_ON, Timestamp.from(userLogin.getCreatedOn()),
                        FIELD_EXPIRES_ON, Timestamp.from(userLogin.getExpiresOn())));
        return updateCount == 1;
    }

    public boolean delete(UserLogin userLogin) {
        var updateCount = jdbcTemplate.update(
                "delete from " + TABLE_NAME + " where " + FIELD_AUTH_TOKEN + " = :" + FIELD_AUTH_TOKEN,
                Map.of(FIELD_AUTH_TOKEN, userLogin.getAuthToken()));
        return updateCount == 1;
    }

    public int deleteAllForUserIdAndAuthToken(long userId, String authToken) {
        var updateCount = jdbcTemplate.update(
                "delete from " + TABLE_NAME + " where "
                        + FIELD_USER_ID + " = :" + FIELD_USER_ID + " and "
                        + FIELD_AUTH_TOKEN + " = :" + FIELD_AUTH_TOKEN,
                Map.of(FIELD_USER_ID, userId, FIELD_AUTH_TOKEN, authToken));
        return updateCount;
    }

    public int deleteAllForUserId(long userId) {
        var updateCount = jdbcTemplate.update(
                "delete from " + TABLE_NAME + " where " + FIELD_USER_ID + " = :" + FIELD_USER_ID,
                Map.of(FIELD_USER_ID, userId));
        return updateCount;
    }


    private final String TABLE_NAME = "user_login";

    private final String FIELD_ID = "id";
    private final String FIELD_USER_ID = "user_id";
    private final String FIELD_AUTH_TOKEN = "auth_token";
    private final String FIELD_CREATED_ON = "created_on";
    private final String FIELD_EXPIRES_ON = "expires_on";

    private UserLogin mapRow(ResultSet rs, int rowNum) throws SQLException {
        return UserLogin.builder()
                .id(rs.getLong(FIELD_ID))
                .userId(rs.getLong(FIELD_USER_ID))
                .authToken(rs.getString(FIELD_AUTH_TOKEN))
                .createdOn(rs.getTimestamp(FIELD_CREATED_ON).toInstant())
                .expiresOn(rs.getTimestamp(FIELD_EXPIRES_ON).toInstant())
                .build();
    }

}
