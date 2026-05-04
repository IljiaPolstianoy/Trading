CREATE TABLE IF NOT EXISTS pattern
(
    id SERIAL PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS pattern_candle_directions
(
    pattern_id        INTEGER NOT NULL,
    position          INTEGER NOT NULL,
    candle_directions BOOLEAN NOT NULL,
    FOREIGN KEY (pattern_id) REFERENCES pattern (id),
    PRIMARY KEY (pattern_id, position)
);