package demo.newsarticles.projection;


import java.sql.Connection;
import java.sql.SQLException;

import akka.japi.function.Function;
import akka.projection.jdbc.JdbcSession;
import org.jdbi.v3.core.CloseException;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

public class ProjectionJdbcSession implements JdbcSession {
    private final Connection connection;

    public ProjectionJdbcSession(Jdbi jdbi) {
        try {
            Handle handle = null;
            try {
                handle = jdbi.open();
            } catch (Exception err) {
                if (handle != null) {
                    try {
                        handle.close();
                    } catch (CloseException closureErr) {
                        //log.error("Failed to close handle after having partially failed to open - please check for resource leaks!", closureErr);
                    }
                }
                throw new Exception("Failed to open JDBI handle for projection ");
            }

            this.connection = handle.getConnection();

            connection.setAutoCommit(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <Result> Result withConnection(Function<Connection, Result> func) throws Exception {
        return func.apply(connection);
    }

    @Override
    public void commit() throws SQLException{
        connection.commit();
    }

    @Override
    public void rollback() throws SQLException {
        connection.rollback();
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }
}
