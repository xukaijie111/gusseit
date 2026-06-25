package com.guseeit.service;

import com.guseeit.domain.GenerationJob;
import com.guseeit.domain.JobStatus;
import com.guseeit.repository.GenerationJobRepository;
import com.guseeit.support.DynastyConstants;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JobService {

    private final GenerationJobRepository jobRepository;
    private final GenerationService generationService;
    private final Set<String> runningDynasties = ConcurrentHashMap.newKeySet();

    public JobService(GenerationJobRepository jobRepository, GenerationService generationService) {
        this.jobRepository = jobRepository;
        this.generationService = generationService;
    }

    public List<String> supportedDynasties() {
        return DynastyConstants.SUPPORTED;
    }

    public List<GenerationJob> listRecent(int limit) {
        return jobRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit));
    }

    public GenerationJob getJob(String id) {
        GenerationJob job = jobRepository.findById(id).orElse(null);
        if (job == null) {
            throw new IllegalArgumentException("任务不存在");
        }
        return job;
    }

    @Transactional
    public GenerationJob createJob(String dynasty, int count) {
        if (!DynastyConstants.SUPPORTED.contains(dynasty)) {
            throw new IllegalArgumentException("无效朝代");
        }
        if (count < 1 || count > 20) {
            throw new IllegalArgumentException("数量须为 1–20");
        }
        if (runningDynasties.contains(dynasty)) {
            throw new IllegalStateException("该朝代已有任务在运行，请稍后再试");
        }

        GenerationJob job = new GenerationJob();
        job.setDynasty(dynasty);
        job.setTargetCount(count);
        job.setStatus(JobStatus.pending);
        return jobRepository.save(job);
    }

    @Transactional
    public void updateMessage(String jobId, String message) {
        GenerationJob job = jobRepository.findById(jobId).orElse(null);
        if (job != null) {
            job.setMessage(message);
            jobRepository.save(job);
        }
    }

    @Transactional
    public void markRunning(String jobId) {
        GenerationJob job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            throw new IllegalStateException("任务不存在");
        }
        job.setStatus(JobStatus.running);
        job.setMessage("已开始生成");
        jobRepository.save(job);
    }

    @Transactional
    public void finishJob(String jobId, JobStatus status, int successCount, int failCount, String message) {
        GenerationJob job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            throw new IllegalStateException("任务不存在");
        }
        job.setStatus(status);
        job.setSuccessCount(successCount);
        job.setFailCount(failCount);
        job.setMessage(message);
        job.setFinishedAt(LocalDateTime.now());
        jobRepository.save(job);
    }

    @Async("generationExecutor")
    public void runJobAsync(final String jobId, final String dynasty, final int count) {
        if (!runningDynasties.add(dynasty)) {
            finishJob(jobId, JobStatus.failed, 0, 0, "该朝代已有任务在运行");
            return;
        }

        try {
            markRunning(jobId);
            GenerationService.GenerationResult result = generationService.runGeneration(
                    dynasty,
                    count,
                    new GenerationService.ProgressCallback() {
                        @Override
                        public void onProgress(String message) {
                            updateMessage(jobId, message);
                        }
                    }
            );
            finishJob(
                    jobId,
                    JobStatus.completed,
                    result.getSuccessCount(),
                    result.getFailCount(),
                    "完成：成功 " + result.getSuccessCount() + "，失败 " + result.getFailCount()
            );
        } catch (Exception e) {
            finishJob(jobId, JobStatus.failed, 0, 0, e.getMessage());
        } finally {
            runningDynasties.remove(dynasty);
        }
    }
}
