package com.guseeit.controller;

import com.guseeit.domain.AnecdoteData;
import com.guseeit.domain.AnecdoteDraft;
import com.guseeit.domain.AnecdoteImage;
import com.guseeit.dto.AnecdoteItemDto;
import com.guseeit.service.AnecdoteService;
import com.guseeit.support.DynastyConstants;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final AnecdoteService anecdoteService;

    public ApiController(AnecdoteService anecdoteService) {
        this.anecdoteService = anecdoteService;
    }

    @GetMapping("/dynasties")
    public List<Map<String, Object>> dynasties() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (DynastyConstants.Dynasty d : DynastyConstants.SUPPORTED) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", d.getId());
            m.put("name", d.getName());
            list.add(m);
        }
        return list;
    }

    // ==================== 典故生成 → 草稿 ====================

    @SuppressWarnings("unchecked")
    @PostMapping("/admin/anecdotes/generate")
    public ResponseEntity<Map<String, Object>> generateAnecdotes(@RequestBody Map<String, Object> body) {
        try {
            Integer dynastyId = toInt(body.get("dynasty_id"));
            int count = toInt(body.getOrDefault("count", 10));
            if (dynastyId == null) return bad("朝代不能为空");
            AnecdoteService.DraftGenResult result = anecdoteService.generateAndDraft(dynastyId, count);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("anecdotes", result.getSaved());
            resp.put("total", result.getSaved().size());
            resp.put("errors", result.getErrors());
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) { return bad(e.getMessage()); }
        catch (Exception e) { return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap(e.getMessage())); }
    }

    // ==================== 草稿 ====================

    @GetMapping("/admin/anecdotes/drafts")
    public ResponseEntity<Map<String, Object>> listDrafts(
            @RequestParam(required = false) Integer dynastyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AnecdoteDraft> result = anecdoteService.listDrafts(dynastyId, page, size);
        List<AnecdoteItemDto> rows = new ArrayList<>();
        for (AnecdoteDraft d : result.getContent()) {
            AnecdoteItemDto dto = new AnecdoteItemDto();
            dto.setId(d.getId());
            dto.setDynastyId(d.getDynastyId());
            dto.setDynastyName(DynastyConstants.toName(d.getDynastyId()));
            dto.setAnecdoteName(d.getAnecdoteName());
            dto.setSummary(d.getSummary());
            dto.setHistoricalPlace(d.getHistoricalPlace());
            dto.setModernLocation(d.getModernLocation());
            dto.setModernCity(d.getModernCity());
            dto.setLatitude(d.getLatitude());
            dto.setLongitude(d.getLongitude());
            rows.add(dto);
        }
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("rows", rows);
        resp.put("total", result.getTotalElements());
        resp.put("page", page);
        resp.put("size", size);
        return ResponseEntity.ok(resp);
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/admin/anecdotes/drafts/approve")
    public ResponseEntity<Map<String, Object>> approveDrafts(@RequestBody Map<String, Object> body) {
        List<Long> ids = toLongList((List<Object>) body.getOrDefault("ids", new ArrayList<>()));
        if (ids.isEmpty()) return bad("请选择至少一条");
        int c = anecdoteService.approveDrafts(ids);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("approved", c);
        r.put("message", "成功入库 " + c + " 条");
        return ResponseEntity.ok(r);
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/admin/anecdotes/drafts/reject")
    public ResponseEntity<Map<String, Object>> rejectDrafts(@RequestBody Map<String, Object> body) {
        List<Long> ids = toLongList((List<Object>) body.getOrDefault("ids", new ArrayList<>()));
        if (ids.isEmpty()) return bad("请选择至少一条");
        int c = anecdoteService.rejectDrafts(ids);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("deleted", c);
        r.put("message", "已删除 " + c + " 条草稿");
        return ResponseEntity.ok(r);
    }

    // ==================== 典故库 ====================

    @GetMapping("/admin/anecdotes")
    public ResponseEntity<Map<String, Object>> listAnecdotes(
            @RequestParam(required = false) Integer dynastyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AnecdoteData> result = anecdoteService.list(dynastyId, page, size);
        List<AnecdoteItemDto> rows = new ArrayList<>();
        for (AnecdoteData d : result.getContent()) {
            AnecdoteItemDto dto = new AnecdoteItemDto();
            dto.setId(d.getId());
            dto.setDynastyId(d.getDynastyId());
            dto.setDynastyName(DynastyConstants.toName(d.getDynastyId()));
            dto.setAnecdoteName(d.getAnecdoteName());
            dto.setSummary(d.getSummary());
            dto.setHistoricalPlace(d.getHistoricalPlace());
            dto.setModernLocation(d.getModernLocation());
            dto.setModernCity(d.getModernCity());
            dto.setLatitude(d.getLatitude());
            dto.setLongitude(d.getLongitude());
            rows.add(dto);
        }
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("rows", rows);
        resp.put("total", result.getTotalElements());
        resp.put("page", page);
        resp.put("size", size);
        return ResponseEntity.ok(resp);
    }

    // ==================== 题库（图片） ====================

    @GetMapping("/admin/anecdotes/images")
    public ResponseEntity<Map<String, Object>> listImages(
            @RequestParam(required = false) Integer dynastyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AnecdoteImage> result = anecdoteService.listImages(dynastyId, page, size);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AnecdoteImage img : result.getContent()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", img.getId());
            row.put("dynastyId", img.getDynastyId());
            row.put("dynastyName", DynastyConstants.toName(img.getDynastyId()));
            row.put("anecdoteName", img.getAnecdoteName());
            row.put("summary", img.getSummary());
            row.put("historicalPlace", img.getHistoricalPlace());
            row.put("modernCity", img.getModernCity());
            row.put("imageUrl", img.getImageUrl());
            row.put("imageSize", img.getImageSize());
            rows.add(row);
        }
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("rows", rows);
        resp.put("total", result.getTotalElements());
        resp.put("page", page);
        resp.put("size", size);
        return ResponseEntity.ok(resp);
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/admin/anecdotes/generate-images")
    public ResponseEntity<Map<String, Object>> generateImages(@RequestBody Map<String, Object> body) {
        List<Long> ids = toLongList((List<Object>) body.getOrDefault("ids", new ArrayList<>()));
        if (ids.isEmpty()) return bad("请选择至少一条典故");
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("message", "开始生成 " + ids.size() + " 张图片");
        try {
            List<String> progress = new ArrayList<>();
            int[] r = anecdoteService.generateImages(ids, progress::add);
            resp.put("success", r[0]);
            resp.put("fail", r[1]);
            resp.put("progress", progress);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap(e.getMessage()));
        }
        return ResponseEntity.ok(resp);
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/admin/anecdotes/images/delete")
    public ResponseEntity<Map<String, Object>> deleteImages(@RequestBody Map<String, Object> body) {
        List<Long> ids = toLongList((List<Object>) body.getOrDefault("ids", new ArrayList<>()));
        if (ids.isEmpty()) return bad("请选择至少一条");
        int c = anecdoteService.deleteImages(ids);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("deleted", c);
        r.put("message", "已删除 " + c + " 条题目");
        return ResponseEntity.ok(r);
    }

    private static Integer toInt(Object v) {
        if (v instanceof Number) return ((Number) v).intValue();
        if (v instanceof String) try { return Integer.parseInt((String) v); } catch (NumberFormatException e) { return null; }
        return null;
    }

    private static List<Long> toLongList(List<Object> raw) {
        List<Long> ids = new ArrayList<>();
        for (Object o : raw) { if (o instanceof Number) ids.add(((Number) o).longValue()); }
        return ids;
    }

    private static ResponseEntity<Map<String, Object>> bad(String msg) {
        return ResponseEntity.badRequest().body(errorMap(msg));
    }

    private static Map<String, Object> errorMap(String msg) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("error", msg);
        return m;
    }
}
