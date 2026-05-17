package com.irongate.service;

import com.irongate.dao.ActivityLogDAO;
import com.irongate.dao.FileDAO;
import com.irongate.model.VaultFile;
import com.irongate.security.AESUtil;
import com.irongate.security.HashUtil;
import com.irongate.util.SessionManager;

import java.io.*;
import java.nio.file.*;
import java.util.List;

public class FileService {

    private static final String VAULT_DIR = "vault_storage";

    private final FileDAO        fileDAO = new FileDAO();
    private final ActivityLogDAO logDAO  = new ActivityLogDAO();

    public enum UploadResult { SUCCESS, DUPLICATE, ERROR }

    public record UploadOutcome(UploadResult result, String duplicateMatchName) {}

    // ── Normal upload ────────────────────────────────────────────

    public UploadOutcome upload(File sourceFile) {
        if (!SessionManager.isLoggedIn()) return new UploadOutcome(UploadResult.ERROR, null);
        try {
            int    uid    = SessionManager.getCurrentUser().getUserId();
            String sesKey = SessionManager.getSessionKey();

            byte[] plaintext = Files.readAllBytes(sourceFile.toPath());
            String hash      = HashUtil.sha256(plaintext);

            if (fileDAO.isDuplicate(uid, hash)) {
                return new UploadOutcome(UploadResult.DUPLICATE, sourceFile.getName());
            }

            byte[] ciphertext = AESUtil.encrypt(plaintext, sesKey);

            Path vaultDir = Paths.get(VAULT_DIR, String.valueOf(uid));
            Files.createDirectories(vaultDir);
            String storedName = System.currentTimeMillis() + "_" + sourceFile.getName() + ".enc";
            Path   destPath   = vaultDir.resolve(storedName);
            Files.write(destPath, ciphertext);

            String ext    = getExtension(sourceFile.getName());
            int    fileId = fileDAO.insertFile(uid, sourceFile.getName(), ext,
                                               destPath.toString(), plaintext.length);
            fileDAO.insertHash(fileId, hash);
            logDAO.log(uid, fileId, "UPLOAD");

            return new UploadOutcome(UploadResult.SUCCESS, null);

        } catch (Exception e) {
            e.printStackTrace();
            return new UploadOutcome(UploadResult.ERROR, null);
        }
    }

    // ── Force upload duplicate (user clicked Keep Anyway) ────────

    public UploadOutcome forceUpload(File sourceFile) {
        if (!SessionManager.isLoggedIn()) return new UploadOutcome(UploadResult.ERROR, null);
        try {
            int    uid    = SessionManager.getCurrentUser().getUserId();
            String sesKey = SessionManager.getSessionKey();

            byte[] plaintext  = Files.readAllBytes(sourceFile.toPath());
            byte[] ciphertext = AESUtil.encrypt(plaintext, sesKey);

            // Make hash unique so it won't match existing entries
            String hash = HashUtil.sha256(plaintext) + "_dup_" + System.currentTimeMillis();

            Path vaultDir = Paths.get(VAULT_DIR, String.valueOf(uid));
            Files.createDirectories(vaultDir);
            String storedName = System.currentTimeMillis() + "_copy_" + sourceFile.getName() + ".enc";
            Path   destPath   = vaultDir.resolve(storedName);
            Files.write(destPath, ciphertext);

            String ext      = getExtension(sourceFile.getName());
            String dispName = sourceFile.getName().replaceFirst("(\\.[^.]+)$", " (copy)$1");
            int    fileId   = fileDAO.insertFile(uid, dispName, ext,
                                                  destPath.toString(), plaintext.length);
            fileDAO.insertHash(fileId, hash);
            logDAO.log(uid, fileId, "UPLOAD_DUPLICATE_FORCED");

            return new UploadOutcome(UploadResult.SUCCESS, null);

        } catch (Exception e) {
            e.printStackTrace();
            return new UploadOutcome(UploadResult.ERROR, null);
        }
    }

    // ── Decrypt to memory (View) ─────────────────────────────────

    public byte[] decryptToMemory(VaultFile vf) {
        if (!SessionManager.isLoggedIn()) return null;
        try {
            byte[] ciphertext = Files.readAllBytes(Paths.get(vf.getFilePath()));
            byte[] plaintext  = AESUtil.decrypt(ciphertext, SessionManager.getSessionKey());
            logDAO.log(vf.getUserId(), vf.getFileId(), "VIEW");
            return plaintext;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ── Download (decrypt + save to disk) ───────────────────────

    public boolean downloadTo(VaultFile vf, File destination) {
        byte[] data = decryptToMemory(vf);
        if (data == null) return false;
        try {
            Files.write(destination.toPath(), data);
            logDAO.log(vf.getUserId(), vf.getFileId(), "DOWNLOAD");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Rename display name ──────────────────────────────────────

    public boolean renameFile(VaultFile vf, String newName) {
        return fileDAO.renameFile(vf.getFileId(), newName);
    }

    // ── Soft delete ──────────────────────────────────────────────

    public void delete(VaultFile vf) {
        fileDAO.softDelete(vf.getFileId());
        logDAO.log(vf.getUserId(), vf.getFileId(), "DELETE");
    }

    // ── Queries ──────────────────────────────────────────────────

    public List<VaultFile> getMyFiles() {
        if (!SessionManager.isLoggedIn()) return List.of();
        return fileDAO.getActiveFiles(SessionManager.getCurrentUser().getUserId());
    }

    public List<VaultFile> getDuplicates() {
        if (!SessionManager.isLoggedIn()) return List.of();
        return fileDAO.getDuplicates(SessionManager.getCurrentUser().getUserId());
    }

    public long[] getStats() {
        if (!SessionManager.isLoggedIn()) return new long[4];
        return fileDAO.getStats(SessionManager.getCurrentUser().getUserId());
    }

    // ── Helpers ──────────────────────────────────────────────────

    private String getExtension(String name) {
        int dot = name.lastIndexOf('.');
        return (dot >= 0) ? name.substring(dot + 1).toLowerCase() : "bin";
    }
}