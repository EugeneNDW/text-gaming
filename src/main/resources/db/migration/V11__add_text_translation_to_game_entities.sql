CREATE TABLE texts (
    id BIGSERIAL PRIMARY KEY
);

CREATE TABLE text_translations (
    text_id BIGINT NOT NULL,
    language_code VARCHAR(10) NOT NULL,
    translated_text TEXT NOT NULL,
    PRIMARY KEY (text_id, language_code),
    FOREIGN KEY (text_id) REFERENCES texts(id)
);

ALTER TABLE conversations DROP COLUMN conversation_text;

ALTER TABLE conversations
ADD COLUMN text_id BIGINT NOT NULL,
ADD FOREIGN KEY (text_id) REFERENCES texts(id);

ALTER TABLE options DROP COLUMN option_text;

ALTER TABLE options
ADD COLUMN text_id BIGINT NOT NULL,
ADD FOREIGN KEY (text_id) REFERENCES texts(id) ON DELETE CASCADE;