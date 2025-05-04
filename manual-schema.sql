-- USERS
CREATE TABLE users
(
    id         SERIAL PRIMARY KEY,
    username   TEXT UNIQUE NOT NULL,
    password   TEXT        NOT NULL,
    role       TEXT      DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- KYC (Know Your Customer)
CREATE TABLE kyc
(
    id            SERIAL PRIMARY KEY,
    user_id       BIGINT UNIQUE REFERENCES users (id),
    full_name     TEXT        NOT NULL,
    phone         TEXT,
    email         TEXT UNIQUE NOT NULL,
    civil_id      TEXT UNIQUE,
    address       TEXT,
    date_of_birth DATE,
    verified      BOOLEAN DEFAULT FALSE
);

-- ACCOUNTS
CREATE TABLE accounts
(
    id             SERIAL PRIMARY KEY,
    user_id        BIGINT REFERENCES users (id),
    account_number TEXT UNIQUE NOT NULL,
    account_type   TEXT        NOT NULL, -- 'MAIN' or 'SAVINGS'
    balance        DECIMAL(9, 3) DEFAULT 0,
    currency       TEXT          DEFAULT 'KWD',
    created_at     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    is_active      BOOLEAN       DEFAULT TRUE
);

-- POTS (Sub-accounts of MAIN account)
CREATE TABLE pots
(
    id               SERIAL PRIMARY KEY,
    account_id       BIGINT REFERENCES accounts (id),
    name             TEXT          NOT NULL,
    balance          DECIMAL(9, 3) DEFAULT 0,
    allocation_type  TEXT          NOT NULL, -- 'PERCENT' or 'FIXED'
    allocation_value DECIMAL(9, 3) NOT NULL,
    created_at       TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

-- CARDS
CREATE TABLE cards
(
    id          SERIAL PRIMARY KEY,
    account_id  BIGINT REFERENCES accounts (id),
    pot_id      BIGINT REFERENCES pots (id), -- NULL for main account cards
    card_number TEXT,                        -- NULL for tokenized cards
    token       TEXT,                        -- NULL for physical cards
    card_type   TEXT NOT NULL,               -- 'PHYSICAL' or 'TOKEN'
    is_active   BOOLEAN   DEFAULT true,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at  TIMESTAMP
);

-- TRANSACTIONS
CREATE TABLE transactions
(
    id               SERIAL PRIMARY KEY,
    source_id        BIGINT,                 -- References accounts(id) or pots(id)
    card_id          BIGINT REFERENCES cards (id),
    amount           DECIMAL(9, 3) NOT NULL,
    transaction_type TEXT          NOT NULL, -- 'DEPOSIT', 'WITHDRAWAL', 'TRANSFER', 'PURCHASE'
    description      TEXT,
    balance_before   DECIMAL(9, 3) NOT NULL,
    balance_after    DECIMAL(9, 3) NOT NULL,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);