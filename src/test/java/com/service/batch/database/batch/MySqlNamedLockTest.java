package com.service.batch.database.batch;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MySqlNamedLockTest {

    @Test
    void runsOnlyWhileNamedLockIsHeld() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement acquireStatement = mock(PreparedStatement.class);
        PreparedStatement releaseStatement = mock(PreparedStatement.class);
        ResultSet acquireResult = result(1);
        ResultSet releaseResult = result(1);
        AtomicBoolean ran = new AtomicBoolean();

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT GET_LOCK(?, 0)")).thenReturn(acquireStatement);
        when(connection.prepareStatement("SELECT RELEASE_LOCK(?)")).thenReturn(releaseStatement);
        when(acquireStatement.executeQuery()).thenReturn(acquireResult);
        when(releaseStatement.executeQuery()).thenReturn(releaseResult);

        boolean acquired = new MySqlNamedLock(dataSource).runIfAcquired("job", () -> {
            ran.set(true);
            return null;
        });

        assertThat(acquired).isTrue();
        assertThat(ran).isTrue();
        verify(releaseStatement).executeQuery();
    }

    @Test
    void skipsWhenAnotherInstanceHoldsTheLock() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement acquireStatement = mock(PreparedStatement.class);
        ResultSet acquireResult = result(0);
        AtomicBoolean ran = new AtomicBoolean();

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT GET_LOCK(?, 0)")).thenReturn(acquireStatement);
        when(acquireStatement.executeQuery()).thenReturn(acquireResult);

        boolean acquired = new MySqlNamedLock(dataSource).runIfAcquired("job", () -> {
            ran.set(true);
            return null;
        });

        assertThat(acquired).isFalse();
        assertThat(ran).isFalse();
        verify(connection, never()).prepareStatement("SELECT RELEASE_LOCK(?)");
    }

    @Test
    void acquiresBeforeSubmittingAndReleasesAfterTheTask() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement acquireStatement = mock(PreparedStatement.class);
        PreparedStatement releaseStatement = mock(PreparedStatement.class);
        ResultSet acquireResult = result(1);
        ResultSet releaseResult = result(1);
        AtomicBoolean ran = new AtomicBoolean();

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT GET_LOCK(?, 0)")).thenReturn(acquireStatement);
        when(connection.prepareStatement("SELECT RELEASE_LOCK(?)")).thenReturn(releaseStatement);
        when(acquireStatement.executeQuery()).thenReturn(acquireResult);
        when(releaseStatement.executeQuery()).thenReturn(releaseResult);
        Executor directExecutor = Runnable::run;

        boolean accepted = new MySqlNamedLock(dataSource).submitIfAcquired("job", directExecutor, () -> {
            ran.set(true);
            return null;
        });

        assertThat(accepted).isTrue();
        assertThat(ran).isTrue();
        verify(releaseStatement).executeQuery();
        verify(connection).close();
    }

    private static ResultSet result(int value) throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(value);
        return resultSet;
    }
}
