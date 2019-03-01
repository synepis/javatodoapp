package io.github.synepis.todo.utils;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class SqlUtils {

    public static Long mapCountRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getLong(1);
    }

    public static Timestamp mapNullableInstant(Instant instant) {
        return instant != null ? Timestamp.from(instant) : null;
    }

    public static Instant mapNullableTimestamp(Timestamp timestamp) {
        return timestamp != null ? timestamp.toInstant() : null;
    }

    public static SqlParameterSource parameterMap(Object... args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Number of arguments must be even");
        }

        var source = new MapSqlParameterSource();
        for (int i = 0; i < args.length; i += 2) {
            source.addValue((String) args[i], args[i + 1]);
        }
        return source;
    }
}
