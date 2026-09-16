CREATE TABLE IF NOT EXISTS client (
    client_id INTEGER PRIMARY KEY,
    client_name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS instrument (
    instrument_id SERIAL PRIMARY KEY,
    instrument_ticker VARCHAR(20) NOT NULL UNIQUE,
    instrument_name VARCHAR(255) NOT NULL,
    asset_class VARCHAR(20) NOT NULL,
    instrument_status VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS account (
    account_id SERIAL PRIMARY KEY,
    account_number VARCHAR(32) NOT NULL UNIQUE,
    opening_date DATE NOT NULL,
    balance NUMERIC(19,2) NOT NULL,
    purchasing_power NUMERIC(19,2) NOT NULL,
    account_status VARCHAR(20) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    version BIGINT NOT NULL,
    suspended_at TIMESTAMPTZ NULL,
    closed_at TIMESTAMPTZ NULL,
    client_id INTEGER NOT NULL REFERENCES client(client_id)
);

CREATE TABLE IF NOT EXISTS holdings (
    holding_id SERIAL PRIMARY KEY,
    quantity BIGINT NOT NULL,
    purchase_price NUMERIC(19,2) NOT NULL,
    account_id INTEGER NOT NULL REFERENCES account(account_id),
    instrument_id INTEGER NOT NULL REFERENCES instrument(instrument_id),
    CONSTRAINT uk_holdings_account_instrument UNIQUE (account_id, instrument_id)
);

CREATE TABLE IF NOT EXISTS orders (
    order_id SERIAL PRIMARY KEY,
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    order_status VARCHAR(20) NOT NULL,
    received_at TIMESTAMPTZ NOT NULL,
    order_type VARCHAR(10) NOT NULL,
    price NUMERIC(19,2) NULL,
    quantity BIGINT NOT NULL,
    transaction_date TIMESTAMPTZ NULL,
    account_id INTEGER NOT NULL REFERENCES account(account_id),
    instrument_id INTEGER NOT NULL REFERENCES instrument(instrument_id)
);

