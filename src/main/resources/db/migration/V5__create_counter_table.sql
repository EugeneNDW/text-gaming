CREATE TABLE counter (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE game_counter (
    id BIGSERIAL PRIMARY KEY,
    game_id BIGINT NOT NULL REFERENCES game_state(id),
    counter_id BIGINT NOT NULL REFERENCES counter(id),
    counter_value INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (game_id, counter_id)
);

CREATE INDEX game_counter_game_id_idx ON game_counter(game_id);