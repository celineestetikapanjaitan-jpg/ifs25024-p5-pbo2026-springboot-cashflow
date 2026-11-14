package org.delcom.ifs25024springbootcashflow.adapter.repository;

import org.delcom.ifs25024springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25024springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25024springbootcashflow.domain.repository.ITransactionRepository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Implementasi JDBC (PostgreSQL) untuk {@link ITransactionRepository}.
 */
public class JdbcTransactionRepository implements ITransactionRepository {

    private static final String COLUMNS = "id, user_id, description, amount, type, proof_path, created_at";

    private final DataSource dataSource;

    public JdbcTransactionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Transaction> findAll() {
        return query("SELECT " + COLUMNS + " FROM transactions ORDER BY id", null);
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return query("SELECT " + COLUMNS + " FROM transactions WHERE user_id = ? ORDER BY id", userId);
    }

    @Override
    public Optional<Transaction> findById(Integer id) {
        return JdbcSupport.execute(dataSource, connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT " + COLUMNS + " FROM transactions WHERE id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(map(rs)) : Optional.empty();
                }
            }
        });
    }

    @Override
    public Transaction save(Transaction entity) {
        return insert(entity.getUserId(), entity.getType(), entity.getDescription(), entity.getAmount(),
                entity.getProofPath(), entity.getCreatedAt());
    }

    @Override
    public Transaction save(int userId, TransactionType type, String description, long amount, String proofPath) {
        return insert(userId, type, description, amount, proofPath, LocalDateTime.now());
    }

    @Override
    public boolean deleteById(Integer id) {
        return JdbcSupport.execute(dataSource, connection -> {
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM transactions WHERE id = ?")) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        });
    }

    @Override
    public void update(Transaction entity) {
        JdbcSupport.execute(dataSource, connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE transactions SET description = ?, amount = ?, proof_path = ? WHERE id = ?")) {
                ps.setString(1, entity.getDescription());
                ps.setLong(2, entity.getAmount());
                ps.setString(3, entity.getProofPath());
                ps.setInt(4, entity.getId());
                return ps.executeUpdate();
            }
        });
    }

    @Override
    public List<Transaction> findBy(Predicate<Transaction> predicate) {
        return findAll().stream().filter(predicate).toList();
    }

    /** Menjalankan SELECT daftar; {@code userId} null berarti tanpa parameter. */
    private List<Transaction> query(String sql, Integer userId) {
        return JdbcSupport.execute(dataSource, connection -> {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                if (userId != null) {
                    ps.setInt(1, userId);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    List<Transaction> result = new ArrayList<>();
                    while (rs.next()) {
                        result.add(map(rs));
                    }
                    return result;
                }
            }
        });
    }

    private Transaction insert(int userId, TransactionType type, String description, long amount,
                               String proofPath, LocalDateTime createdAt) {
        return JdbcSupport.execute(dataSource, connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO transactions (user_id, description, amount, type, proof_path, created_at)"
                            + " VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setString(2, description);
                ps.setLong(3, amount);
                ps.setString(4, type.name());
                ps.setString(5, proofPath);
                ps.setTimestamp(6, Timestamp.valueOf(createdAt));
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    return new Transaction(keys.getInt(1), userId, description, amount, type, proofPath, createdAt);
                }
            }
        });
    }

    private static Transaction map(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("description"),
                rs.getLong("amount"),
                TransactionType.valueOf(rs.getString("type")),
                rs.getString("proof_path"),
                rs.getTimestamp("created_at").toLocalDateTime());
    }
}
