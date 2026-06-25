CREATE DATABASE IF NOT EXISTS guseeit DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE guseeit;

CREATE TABLE IF NOT EXISTS rounds (
  id CHAR(36) PRIMARY KEY,
  dynasty VARCHAR(16) NOT NULL COMMENT '朝代',
  location_name VARCHAR(128) NOT NULL COMMENT '历史地名',
  modern_place VARCHAR(128) NULL COMMENT '今地名',
  geo_query VARCHAR(64) NULL COMMENT '地理编码查询词',
  year_ad INT NOT NULL COMMENT '公元年，公元前为负',
  reign_label VARCHAR(64) NULL COMMENT '年号纪年',
  time_label VARCHAR(256) NOT NULL COMMENT '完整时间标注',
  prompt TEXT NOT NULL COMMENT '文生图提示词',
  scene_type VARCHAR(64) NULL COMMENT '历史典故名称（如烽火戏诸侯）',
  knowledge_summary TEXT NULL COMMENT '历史典故正文（100～200字）',
  image_url VARCHAR(512) NULL,
  oss_object_key VARCHAR(256) NULL,
  image_size VARCHAR(32) NULL,
  status ENUM('pending', 'generated', 'failed') NOT NULL DEFAULT 'pending',
  error_message TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_dynasty_location_year (dynasty, location_name, year_ad),
  KEY idx_dynasty (dynasty),
  KEY idx_status (status),
  KEY idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS generation_jobs (
  id CHAR(36) PRIMARY KEY,
  dynasty VARCHAR(16) NOT NULL,
  target_count INT NOT NULL,
  status ENUM('pending', 'running', 'completed', 'failed') NOT NULL DEFAULT 'pending',
  success_count INT NOT NULL DEFAULT 0,
  fail_count INT NOT NULL DEFAULT 0,
  message TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  finished_at TIMESTAMP NULL,
  KEY idx_job_status (status),
  KEY idx_job_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
