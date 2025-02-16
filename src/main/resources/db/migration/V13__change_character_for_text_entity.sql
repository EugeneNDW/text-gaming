ALTER TABLE characters DROP COLUMN name;

ALTER TABLE characters
ADD COLUMN name_text_id BIGINT NOT NULL,
ADD CONSTRAINT fk_characters_name_text FOREIGN KEY (name_text_id) REFERENCES texts (id);

ALTER TABLE characters
ADD CONSTRAINT unique_char_name_text_id UNIQUE (name_text_id);