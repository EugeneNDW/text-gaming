CREATE TABLE characters (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

ALTER TABLE conversations ADD COLUMN character_id BIGINT NOT NULL;

ALTER TABLE conversations DROP COLUMN person;

ALTER TABLE conversations
ADD CONSTRAINT fk_conversation_character
FOREIGN KEY (character_id) REFERENCES characters(id);