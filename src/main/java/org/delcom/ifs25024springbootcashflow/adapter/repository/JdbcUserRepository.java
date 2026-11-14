package org.delcom.ifs25024springbootcashflow.adapter.repository;

import org.delcom.ifs25024springbootcashflow.domain.entity.Role;
import org.delcom.ifs25024springbootcashflow.domain.entity.User;
import org.delcom.ifs25024springbootcashflow.domain.repository.IUserRepository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Implementasi JDBC (PostgreSQL) untuk {@link IUserRepository}.
 */
public class JdbcUserRepository implements IUserRepository {

    private static final String COLUMNS = "id, username, password_hash, full_name, role, photo_path";

    private final DataSource dataSource;

    public JdbcUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<User> findAll() {
        return JdbcSupport.execute(dataSource, connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT " + COLUMNS + " FROM users ORDER BY id");
                 ResultSet rs = ps.executeQuery()) {
                return readAll(rs);
            }
        });
    }

    @Override
    public Optional<User> findById(Integer id) {
        return JdbcSupport.execute(dataSource, connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT " + COLUMNS + " FROM users WHERE id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(map(rs)) : Optional.empty();
                }
            }
        });
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return JdbcSupport.execute(dataSource, connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT " + COLUMNS + " FROM users WHERE username = ?")) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(map(rs)) : Optional.empty();
                }
            }
        });
    }

    @Override
    public User save(User entity) {
        return insert(entity.getUsername(), entity.getPasswordHash(), entity.getFullName(),
                entity.getRole(), entity.getPhotoPath());
    }

    @Override
    public User save(String username, String passwordHash, String fullName, Role role) {
        return insert(username, passwordHash, fullName, role, null);
    }

    @Override
    public boolean deleteById(Integer id) {
        return JdbcSupport.execute(dataSource, connection -> {
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM users WHERE id = ?")) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        });
    }

    @Override
    public void update(User entity) {
        JdbcSupport.execute(dataSource, connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE users SET password_hash = ?, full_name = ?, photo_path = ? WHERE id = ?")) {
                ps.setString(1, entity.getPasswordHash());
                ps.setString(2, entity.getFullName());
                ps.setString(3, entity.getPhotoPath());
                ps.setInt(4, entity.getId());
                return ps.executeUpdate();
            }
        });
    }

    @Override
    public List<User> findBy(Predicate<User> predicate) {
        return findAll().stream().filter(predicate).toList();
    }

    private User insert(String username, String passwordHash, String fullName, Role role, String photoPath) {
        return JdbcSupport.execute(dataSource, connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO users (username, password_hash, full_name, role, photo_path) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username);
                ps.setString(2, passwordHash);
                ps.setString(3, fullName);
                ps.setString(4, role.name());
                ps.setString(5, photoPath);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    return new User(keys.getInt(1), username, passwordHash, fullName, role, photoPath);
                }
            }
        });
    }

    private static List<User> readAll(ResultSet rs) throws SQLException {
        List<User> result = new ArrayList<>();
        while (rs.next()) {
            result.add(map(rs));
        }
        return result;
    }

    private static User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("full_name"),
                Role.valueOf(rs.getString("role")),
                rs.getString("photo_path"));
    }
}
