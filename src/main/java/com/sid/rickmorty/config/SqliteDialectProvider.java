package com.sid.rickmorty.config;

import org.springframework.data.jdbc.repository.config.DialectResolver;
import org.springframework.data.relational.core.dialect.AnsiDialect;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Spring Data JDBC does not ship a built-in SQLite dialect, so we plug one in.
 * AnsiDialect keeps SQL generation compatible with SQLite syntax for this challenge scope.
 */
@Component
public class SqliteDialectProvider implements DialectResolver.JdbcDialectProvider {

    @Override
    public Optional<Dialect> getDialect(JdbcOperations operations) {
        return operations.execute((ConnectionCallback<Optional<Dialect>>) this::resolveDialect);
    }

    private Optional<Dialect> resolveDialect(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        if (metaData != null && "SQLite".equalsIgnoreCase(metaData.getDatabaseProductName())) {
            return Optional.of(AnsiDialect.INSTANCE);
        }
        return Optional.empty();
    }
}
