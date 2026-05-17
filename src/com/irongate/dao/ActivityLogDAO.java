package com.irongate.dao;

import com.irongate.model.ActivityLog;
import com.irongate.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDAO {

    /** Write an immutable log entry. action examples: LOGIN, UPLOAD, VIEW, DELETE, OTP_SENT, 2FA_ENABLE */
    public void log(int userId, Integer fileId, String action) {
        String sql = "INSERT INTO ActivityLog (user_id, file_id, action) VALUES (?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            if (fileId != null) ps.setInt(2, fileId); else ps.setNull(2, Types.INTEGER);
            ps.setString(3, action);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /** Retrieve all log entries for a user, most recent first. */
    public List<ActivityLog> getForUser(int userId) {
        String sql = "SELECT log_id, user_id, file_id, action, timestamp " +
                     "FROM ActivityLog WHERE user_id = ? ORDER BY timestamp DESC";
        List<ActivityLog> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private ActivityLog mapRow(ResultSet rs) throws SQLException {
        ActivityLog al = new ActivityLog();
        al.setLogId(rs.getInt("log_id"));
        al.setUserId(rs.getInt("user_id"));
        int fid = rs.getInt("file_id");
        al.setFileId(rs.wasNull() ? null : fid);
        al.setAction(rs.getString("action"));
        Timestamp ts = rs.getTimestamp("timestamp");
        if (ts != null) al.setTimestamp(ts.toLocalDateTime());

        // Derive a display status from action name
        String action = al.getAction();
        if (action.contains("FAIL") || action.contains("LOCK") || action.contains("DENIED"))
            al.setStatus("Failed");
        else if (action.contains("SCAN") || action.contains("CRACKING"))
            al.setStatus("Running");
        else
            al.setStatus("Success");

        return al;
    }
}
