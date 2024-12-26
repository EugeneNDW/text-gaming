CREATE TABLE game (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    default_language TEXT NOT NULL DEFAULT 'EN'
);

INSERT INTO game (name, default_language) VALUES ('The Navigator', 'EN');
