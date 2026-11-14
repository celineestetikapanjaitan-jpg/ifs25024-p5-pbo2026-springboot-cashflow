package org.delcom.ifs25024springbootcashflow.adapter.repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Helper kecil untuk repository JDBC: membuka koneksi, menjalankan pekerjaan,
 * menutup koneksi, dan membungkus {@link SQLException} menjadi unchecked exception
 * agar port repository di domain tetap bersih dari detail JDBC.
 */
final class JdbcSupport {

    /** Pekerjaan JDBC yang memakai satu koneksi. */
    @FunctionalInterface
    interface SqlWork<R> {
        R apply(Connection connection) throws SQLException;
    }

    private JdbcSupport() {
    }

    static <R> R execute(DataSource dataSource, SqlWork<R> work) {
        try (Connection connection = dataSource.getConnection()) {
            return work.apply(connection);
        } catch (SQLException e) {
            throw new IllegalStateException("Operasi database gagal: " + e.getMessage(), e);
        }
    }
}
