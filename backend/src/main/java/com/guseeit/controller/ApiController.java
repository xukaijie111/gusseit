package com.guseeit.controller;

import com.guseeit.domain.GenerationJob;
import com.guseeit.domain.Round;
import com.guseeit.dto.GenerateRequest;
import com.guseeit.dto.JobView;
import com.guseeit.dto.RoundView;
import com.guseeit.service.JobService;
import com.guseeit.service.RoundService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final JobService jobService;
    private final RoundService roundService;

    public ApiController(JobService jobService, RoundService roundService) {
        this.jobService = jobService;
        this.roundService = roundService;
    }

    @GetMapping("/dynasties")
    public Map<String, List<String>> dynasties() {
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        result.put("dynasties", jobService.supportedDynasties());
        return result;
    }

    @GetMapping("/rounds")
    public Map<String, Object> rounds(
            @RequestParam(required = false) String dynasty,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        Page<Round> page = roundService.list(dynasty, status, limit, offset);
        List<RoundView> rows = new ArrayList<RoundView>();
        for (Round round : page.getContent()) {
            rows.add(RoundView.from(round));
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("total", page.getTotalElements());
        result.put("rows", rows);
        return result;
    }

    @GetMapping("/jobs")
    public Map<String, List<JobView>> jobs(@RequestParam(defaultValue = "10") int limit) {
        int capped = Math.min(Math.max(limit, 1), 50);
        List<JobView> jobs = new ArrayList<JobView>();
        for (GenerationJob job : jobService.listRecent(capped)) {
            jobs.add(JobView.from(job));
        }
        Map<String, List<JobView>> result = new HashMap<String, List<JobView>>();
        result.put("jobs", jobs);
        return result;
    }

    @GetMapping("/jobs/{id}")
    public JobView job(@PathVariable String id) {
        return JobView.from(jobService.getJob(id));
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generate(@Valid @RequestBody GenerateRequest request) {
        try {
            GenerationJob job = jobService.createJob(request.getDynasty(), request.getCount());
            jobService.runJobAsync(job.getId(), request.getDynasty(), request.getCount());

            Map<String, Object> body = new LinkedHashMap<String, Object>();
            body.put("jobId", job.getId());
            body.put("message", "任务已启动");
            return ResponseEntity.ok(body);
        } catch (IllegalStateException e) {
            Map<String, Object> body = new LinkedHashMap<String, Object>();
            body.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        } catch (IllegalArgumentException e) {
            Map<String, Object> body = new LinkedHashMap<String, Object>();
            body.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(body);
        }
    }
}
