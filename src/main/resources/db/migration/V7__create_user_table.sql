CREATE TABLE user_info (
  id BIGSERIAL PRIMARY KEY,
  username TEXT,
  permit BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);