use CRAWLING;

ALTER TABLE CoinEntity
    ADD COLUMN coinSymbol varchar(20) NOT NULL DEFAULT 'BTC' AFTER id;

UPDATE CoinEntity
SET coinSymbol = 'BTC'
WHERE coinSymbol IS NULL OR coinSymbol = '';

CREATE INDEX idx_coin_entity_symbol_id ON CoinEntity (coinSymbol, id);
