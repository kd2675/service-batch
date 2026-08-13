package com.service.batch.database.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

@Slf4j
@Component
@RequiredArgsConstructor
public class MySqlNamedLock {
    @Qualifier("batchMasterDatasource")
    private final DataSource dataSource;

    public boolean runIfAcquired(String lockName, Callable<?> action) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            if (!execute(connection, "SELECT GET_LOCK(?, 0)", lockName)) {
                return false;
            }

            try {
                action.call();
                return true;
            } finally {
                try {
                    execute(connection, "SELECT RELEASE_LOCK(?)", lockName);
                } catch (SQLException e) {
                    log.warn("MySQL named lock release failed: {}", lockName, e);
                }
            }
        }
    }

    public boolean submitIfAcquired(String lockName, Executor executor, Callable<?> action) throws SQLException {
        Connection connection = dataSource.getConnection();
        try {
            if (!execute(connection, "SELECT GET_LOCK(?, 0)", lockName)) {
                connection.close();
                return false;
            }
        } catch (SQLException e) {
            close(connection, lockName);
            throw e;
        }

        try {
            executor.execute(() -> {
                try {
                    action.call();
                } catch (Exception e) {
                    log.error("Task holding MySQL named lock failed: {}", lockName, e);
                } finally {
                    releaseAndClose(connection, lockName);
                }
            });
            return true;
        } catch (RuntimeException e) {
            releaseAndClose(connection, lockName);
            throw e;
        }
    }

    private void releaseAndClose(Connection connection, String lockName) {
        try {
            execute(connection, "SELECT RELEASE_LOCK(?)", lockName);
        } catch (SQLException e) {
            log.warn("MySQL named lock release failed: {}", lockName, e);
        } finally {
            close(connection, lockName);
        }
    }

    private void close(Connection connection, String lockName) {
        try {
            connection.close();
        } catch (SQLException e) {
            log.warn("MySQL named lock connection close failed: {}", lockName, e);
        }
    }

    private boolean execute(Connection connection, String sql, String lockName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, lockName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) == 1;
            }
        }
    }
}
