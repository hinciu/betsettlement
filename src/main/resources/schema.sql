CREATE TABLE bets
(
    id              BIGINT                              NOT NULL AUTO_INCREMENT,
    --     sharding is not configured at the moment, but assuming that user_id is used for sharding to achieve better performance while betting;
    user_id         BIGINT                              NOT NULL,
    event_id        BIGINT                              NOT NULL,
    event_market_id BIGINT                              NOT NULL,
    event_winner_id BIGINT                              NOT NULL,
    bet_amount      DECIMAL(19, 2)                      NOT NULL,
    settled         BOOLEAN   DEFAULT FALSE             NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_settlement_lookup
    ON bets (event_id, event_winner_id);

CREATE TABLE failed_event_outcomes
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id        BIGINT       NOT NULL,
    event_name      VARCHAR(255) NOT NULL,
    event_winner_id BIGINT       NOT NULL,
    error_message   VARCHAR(2000),
    failed_at       TIMESTAMP    NOT NULL,
    status          INT          NOT NULL,
    processed_at    TIMESTAMP
);