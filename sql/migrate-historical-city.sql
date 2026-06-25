-- 历史城市名字段（执行一次）
USE guseeit;

ALTER TABLE rounds
  ADD COLUMN historical_city VARCHAR(64) NULL COMMENT '历史城市名（如长安、洛阳）' AFTER location_name;
