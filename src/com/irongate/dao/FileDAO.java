package com.irongate.dao;

import com.irongate.model.VaultFile;
import com.irongate.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FileDAO {

    /** Insert a new file record. Returns generated file_id or -1. */
    public int insertFile(int userId, String fileName, String fileType,
                          String filePath, long fileSize) {
        String sql = "INSERT INTO Files (user_id, file_name, file_type, file_path, file_size) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, fileName);
            ps.setString(3, fileType);
            ps.setString(4, filePath);
            ps.setLong(5, fileSize);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    /** Insert hash record for a file. */
    public void insertHash(int fileId, String hash) {
        String sql = "INSERT INTO FileHashes (file_id, file_hash) VALUES (?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, fileId);
            ps.setString(2, hash);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /** Returns true if the user already has a file with this hash. */
    public boolean isDuplicate(int userId, String hash) {
        String sql = "SELECT 1 FROM FileHashes fh " +
                     "JOIN Files f ON fh.file_id = f.file_id " +
                     "WHERE f.user_id = ? AND fh.file_hash = ? AND f.file_status = 'active'";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, hash);
            return ps.executeQuery().next();
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /** Fetch all active files for a user. */
    public List<VaultFile> getActiveFiles(int userId) {
        String sql = "SELECT file_id, user_id, file_name, file_type, file_path, " +
                     "file_size, upload_date, last_modified, file_status " +
                     "FROM Files WHERE user_id = ? AND file_status = 'active' " +
                     "ORDER BY upload_date DESC";
        List<VaultFile> files = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) files.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return files;
    }

    /** Fetch all duplicate-flagged files for a user. */
    public List<VaultFile> getDuplicates(int userId) {
        String sql = "SELECT f.file_id, f.user_id, f.file_name, f.file_type, f.file_path, " +
                     "f.file_size, f.upload_date, f.last_modified, f.file_status " +
                     "FROM Files f " +
                     "JOIN FileHashes fh ON f.file_id = fh.file_id " +
                     "WHERE f.user_id = ? AND f.file_status IN ('active','duplicate_flag') " +
                     "AND fh.file_hash IN (" +
                     "  SELECT fh2.file_hash FROM FileHashes fh2 " +
                     "  JOIN Files f2 ON fh2.file_id = f2.file_id " +
                     "  WHERE f2.user_id = ? AND f2.file_status = 'active' " +
                     "  GROUP BY fh2.file_hash HAVING COUNT(*) > 1) " +
                     "ORDER BY fh.file_hash, f.upload_date";
        List<VaultFile> files = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) files.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return files;
    }

    /** Rename the display name of a file. */
    public boolean renameFile(int fileId, String newName) {
        String sql = "UPDATE Files SET file_name = ?, last_modified = GETDATE() WHERE file_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setInt(2, fileId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /** Soft-delete a file (set status = 'deleted'). */
    public void softDelete(int fileId) {
        String sql = "UPDATE Files SET file_status = 'deleted' WHERE file_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, fileId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /** Get file by id. */
    public VaultFile getById(int fileId) {
        String sql = "SELECT file_id, user_id, file_name, file_type, file_path, " +
                     "file_size, upload_date, last_modified, file_status " +
                     "FROM Files WHERE file_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, fileId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /** Count stats for the current user: {totalFiles, encryptedFiles, duplicates, totalBytes} */
    public long[] getStats(int userId) {
        long[] stats = new long[4];
        try (Connection c = DBConnection.getConnection()) {
            // total active files
            PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*), SUM(file_size) FROM Files WHERE user_id = ? AND file_status = 'active'");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) { stats[0] = rs.getLong(1); stats[3] = rs.getLong(2); stats[1] = stats[0]; }

            // duplicate count
            ps = c.prepareStatement(
                "SELECT COUNT(*) FROM Files f JOIN FileHashes fh ON f.file_id = fh.file_id " +
                "WHERE f.user_id = ? AND f.file_status = 'active' " +
                "AND fh.file_hash IN (" +
                "  SELECT fh2.file_hash FROM FileHashes fh2 JOIN Files f2 ON fh2.file_id = f2.file_id " +
                "  WHERE f2.user_id = ? AND f2.file_status = 'active' " +
                "  GROUP BY fh2.file_hash HAVING COUNT(*) > 1)");
            ps.setInt(1, userId); ps.setInt(2, userId);
            rs = ps.executeQuery();
            if (rs.next()) stats[2] = rs.getLong(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return stats;
    }

    private VaultFile mapRow(ResultSet rs) throws SQLException {
        VaultFile f = new VaultFile();
        f.setFileId(rs.getInt("file_id"));
        f.setUserId(rs.getInt("user_id"));
        f.setFileName(rs.getString("file_name"));
        f.setFileType(rs.getString("file_type"));
        f.setFilePath(rs.getString("file_path"));
        f.setFileSize(rs.getLong("file_size"));
        f.setFileStatus(rs.getString("file_status"));
        Timestamp ud = rs.getTimestamp("upload_date");
        if (ud != null) f.setUploadDate(ud.toLocalDateTime());
        Timestamp lm = rs.getTimestamp("last_modified");
        if (lm != null) f.setLastModified(lm.toLocalDateTime());
        return f;
    }
}
