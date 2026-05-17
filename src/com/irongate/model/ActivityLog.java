package com.irongate.model;

import java.time.LocalDateTime;

public class ActivityLog {
    private int           logId;
    private int           userId;
    private Integer       fileId;   // nullable
    private String        action;
    private LocalDateTime timestamp;
    private String        status;   // derived for display

    public ActivityLog() {}

    public int           getLogId()    { return logId; }
    public int           getUserId()   { return userId; }
    public Integer       getFileId()   { return fileId; }
    public String        getAction()   { return action; }
    public LocalDateTime getTimestamp(){ return timestamp; }
    public String        getStatus()   { return status; }

    public void setLogId(int v)              { logId     = v; }
    public void setUserId(int v)             { userId    = v; }
    public void setFileId(Integer v)         { fileId    = v; }
    public void setAction(String v)          { action    = v; }
    public void setTimestamp(LocalDateTime v){ timestamp = v; }
    public void setStatus(String v)          { status    = v; }
}
