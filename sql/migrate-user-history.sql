-- user_histories: round_id → image_id 迁移
-- 用法: mysql -h127.0.0.1 -P3307 -uroot -p guseeit < sql/migrate-user-history.sql

-- 删除无效空记录
DELETE FROM user_histories WHERE (image_id IS NULL OR image_id = 0) AND (round_id IS NULL OR round_id = '');

-- 从 round_id 回填 image_id
UPDATE user_histories
SET image_id = CAST(round_id AS UNSIGNED)
WHERE image_id IS NULL AND round_id REGEXP '^[0-9]+$';

-- 同步 round_id（与 image_id 一致）
UPDATE user_histories
SET round_id = CAST(image_id AS CHAR)
WHERE image_id IS NOT NULL AND (round_id IS NULL OR round_id = '');

-- round_id 改为可空，后续可逐步废弃
ALTER TABLE user_histories MODIFY round_id VARCHAR(64) NULL;

-- 从 anecdote_image 回填历史快照字段
UPDATE user_histories h
JOIN anecdote_image i ON h.image_id = i.id
SET
    h.image_url         = COALESCE(NULLIF(h.image_url, ''), i.image_url),
    h.location_name     = COALESCE(NULLIF(h.location_name, ''), i.historical_place),
    h.modern_place      = COALESCE(NULLIF(h.modern_place, ''), i.modern_city),
    h.anecdote_name     = COALESCE(NULLIF(h.anecdote_name, ''), i.anecdote_name),
    h.knowledge_summary = COALESCE(NULLIF(h.knowledge_summary, ''), i.summary),
    h.answer_city       = COALESCE(NULLIF(h.answer_city, ''), i.modern_city),
    h.answer_lat        = COALESCE(h.answer_lat, i.latitude),
    h.answer_lng        = COALESCE(h.answer_lng, i.longitude),
    h.round_id          = COALESCE(NULLIF(h.round_id, ''), CAST(h.image_id AS CHAR))
WHERE h.image_id IS NOT NULL;
