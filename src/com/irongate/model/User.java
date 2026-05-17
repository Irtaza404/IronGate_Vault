package com.irongate.model;

import java.time.LocalDateTime;

public class User {
    private int userId;
    private String username;
    private String email;
    private String passwordHash;
    private LocalDateTime createdAt;

    public User() {}

    public User(int userId, String username, String email, String passwordHash, LocalDateTime createdAt) {
        this.userId       = userId;
        this.username     = username;
        this.email        = email;
        this.passwordHash = passwordHash;
        this.createdAt    = createdAt;
    }

    // Getters
    public int           getUserId()       { return userId; }
    public String        getUsername()     { return username; }
    public String        getEmail()        { return email; }
    public String        getPasswordHash() { return passwordHash; }
    public LocalDateTime getCreatedAt()    { return createdAt; }

    // Setters
    public void setUserId(int v)            { userId       = v; }
    public void setUsername(String v)       { username     = v; }
    public void setEmail(String v)          { email        = v; }
    public void setPasswordHash(String v)   { passwordHash = v; }
    public void setCreatedAt(LocalDateTime v){ createdAt   = v; }
}
