package com.guseeit.dto;

import com.guseeit.domain.GenerationJob;

import java.time.LocalDateTime;

public class JobView {

    private String id;
    private String dynasty;
    private Integer targetCount;
    private String status;
    private Integer successCount;
    private Integer failCount;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;

    public static JobView from(GenerationJob job) {
        JobView view = new JobView();
        view.id = job.getId();
        view.dynasty = job.getDynasty();
        view.targetCount = job.getTargetCount();
        view.status = job.getStatus().name();
        view.successCount = job.getSuccessCount();
        view.failCount = job.getFailCount();
        view.message = job.getMessage();
        view.createdAt = job.getCreatedAt();
        view.finishedAt = job.getFinishedAt();
        return view;
    }

    public String getId() { return id; }
    public String getDynasty() { return dynasty; }
    public Integer getTargetCount() { return targetCount; }
    public String getStatus() { return status; }
    public Integer getSuccessCount() { return successCount; }
    public Integer getFailCount() { return failCount; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
}
