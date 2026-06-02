package com.ai.hub.dto;

public class BatchJobResult {

    private String batchId;
    private int    totalRead;
    private int    totalSaved;
    private int    totalSkipped;
    private String status;
    private String message;

    public BatchJobResult(String batchId, int totalRead, int totalSaved,
                          int totalSkipped, String status, String message) {
        this.batchId      = batchId;
        this.totalRead    = totalRead;
        this.totalSaved   = totalSaved;
        this.totalSkipped = totalSkipped;
        this.status       = status;
        this.message      = message;
    }

    public String getBatchId()       { return batchId; }
    public int    getTotalRead()     { return totalRead; }
    public int    getTotalSaved()    { return totalSaved; }
    public int    getTotalSkipped()  { return totalSkipped; }
    public String getStatus()        { return status; }
    public String getMessage()       { return message; }
}
