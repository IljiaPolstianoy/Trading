DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'time_frame') THEN
            CREATE TYPE time_frame_enum AS ENUM ('1m', '5m', '15m', '30m', '1h', '4h', '1d');
        END IF;

        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'side') THEN
            CREATE TYPE side_enum AS ENUM ('BULLISH', 'BEARISH');
        END IF;

        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'direction') THEN
            CREATE TYPE direction_enum AS ENUM ('BULLISH', 'BEARISH');
        END IF;
    END
$$;


CREATE TABLE IF NOT EXISTS candle
(
    id          SERIAL PRIMARY KEY,
    time_frame  time_frame      NOT NULL,
    max_price   DECIMAL(20, 10) NOT NULL,
    min_price   DECIMAL(20, 10) NOT NULL,
    open_price  DECIMAL(20, 10) NOT NULL,
    close_price DECIMAL(20, 10) NOT NULL,
    volume      DECIMAL(20, 10) NOT NULL,
    start_time  TIMESTAMP       NOT NULL,
    direction   direction       NOT NULL

);

CREATE TABLE IF NOT EXISTS pattern
(
    id   SERIAL PRIMARY KEY,
    side side NOT NULL
);

CREATE TABLE IF NOT EXISTS pattern_candle
(
    pattern_id INTEGER NOT NULL,
    candle_id  INTEGER NOT NULL,
    PRIMARY KEY (pattern_id, candle_id),
    FOREIGN KEY (pattern_id) REFERENCES pattern (id) ON DELETE CASCADE,
    FOREIGN KEY (candle_id) REFERENCES candle (id) ON DELETE CASCADE
);