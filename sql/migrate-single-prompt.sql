-- 已有库从 positive/negative 两列迁移为单列 prompt（执行一次）
USE guseeit;

ALTER TABLE rounds CHANGE COLUMN positive_prompt prompt TEXT NOT NULL COMMENT '文生图提示词';
ALTER TABLE rounds DROP COLUMN negative_prompt;
