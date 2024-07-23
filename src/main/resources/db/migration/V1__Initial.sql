CREATE TABLE game_state (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    location TEXT NOT NULL,
    current_conversation_id BIGINT NOT NULL
);

CREATE TABLE choices (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE game_choice (
    id BIGSERIAL PRIMARY KEY,
    game_id BIGINT NOT NULL REFERENCES game_state(id),
    choice_id BIGINT NOT NULL REFERENCES choices(id)
);

CREATE TABLE game_history (
    id BIGSERIAL PRIMARY KEY,
    game_id BIGINT NOT NULL REFERENCES game_state(id),
    option_id UUID NOT NULL
);

CREATE INDEX game_choice_game_id_idx ON game_choice(game_id);
CREATE INDEX game_history_game_id_idx ON game_history(game_id);