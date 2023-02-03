CREATE TABLE game_counter (
    id BIGSERIAL PRIMARY KEY,
    game_id BIGINT NOT NULL REFERENCES game_state(id),
    counter_type TEXT NOT NULL,
    counter_value INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (game_id, counter_type)
);

CREATE INDEX game_counter_game_id_idx ON game_counter(game_id);