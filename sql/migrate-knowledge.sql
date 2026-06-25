-- 知识讲解字段（执行一次）
USE guseeit;

ALTER TABLE rounds
  ADD COLUMN knowledge_summary TEXT NULL COMMENT '历史典故正文（100～200字）' AFTER scene_type;
