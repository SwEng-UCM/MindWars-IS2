package persistence;

import model.User;

import java.sql.*;

public class UserRepository {

    public boolean usernameExists(String username) {
        String query = "SELECT 1 FROM users WHERE username = ? LIMIT 1";

        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return (rs.next());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check username", e);
        }

    }

    public boolean emailExists(String username) {
        String query = "SELECT 1 FROM users WHERE email = ? LIMIT 1";

        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return (rs.next());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check username", e);
        }

    }

    public User registerUser(String username, String email, String passwordHash) {
        String query = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, passwordHash);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return new User(id, username, email, passwordHash);
                }
            }
            throw new RuntimeException("User created but no ID returned");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to register user", e);
        }

    }

    public User findByEmail(String email) {
        String query = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("password_Hash"));
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user on email", e);
        }
    }

    public User login(String email, String passwordHash) {
        User user = findByEmail(email);
        if (user == null) {
            return null;
        }
        if (!passwordHash.equals(user.getPasswordHash())) {
            return null;
        }
        return user;
    }

}
