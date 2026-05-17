package com.irongate.dao;

import com.irongate.model.User;

import com.irongate.util.DBConnection;

import java.sql.*;

public class UserDAO {

    /** Insert a new user. Returns generated user_id, or -1 on failure. */
    public int registerUser(String username, String email, String passwordHash) {
        String sql = "INSERT INTO Users (username, email, password_hash) VALUES (?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /** Create a blank UserSecurity row linked to the user. */
    public void createUserSecurity(int userId) {
        String sql = "INSERT INTO UserSecurity (user_id) VALUES (?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Look up user by username. Returns null if not found. */
    public User findByUsername(String username) {
        String sql = "SELECT user_id, username, email, password_hash, created_at FROM Users WHERE username = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Look up user by email. Returns null if not found. */
    public User findByEmail(String email) {
        String sql = "SELECT user_id, username, email, password_hash, created_at FROM Users WHERE email = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Update username. Returns false if new username is already taken. */
    public boolean updateUsername(int userId, String newUsername) {
        if (usernameExists(newUsername)) return false;
        String sql = "UPDATE Users SET username = ? WHERE user_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newUsername);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /** Update email. Returns false if new email is already in use. */
    public boolean updateEmail(int userId, String newEmail) {
        if (emailExists(newEmail)) return false;
        String sql = "UPDATE Users SET email = ? WHERE user_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newEmail);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /** Update password hash. */
    public boolean updatePassword(int userId, String newHash) {
        String sql = "UPDATE Users SET password_hash = ? WHERE user_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /** Check if a username is already taken. */
    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM Users WHERE username = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            return ps.executeQuery().next();
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /** Check if an email is already registered. */
    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM Users WHERE email = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            return ps.executeQuery().next();
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /** Returns true if 2FA is enabled for this user. */
    public boolean is2FAEnabled(int userId) {
        String sql = "SELECT two_factor_enabled FROM UserSecurity WHERE user_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBoolean(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /** Toggle 2FA on/off for a user. */
    public void set2FA(int userId, boolean enabled) {
        String sql = "UPDATE UserSecurity SET two_factor_enabled = ? WHERE user_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, enabled);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) u.setCreatedAt(ts.toLocalDateTime());
        return u;
    }
}
