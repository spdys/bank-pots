create table users
(
    id         serial
        primary key,
    username   text not null
        unique,
    password   text not null,
    role       text      default 'USER'::text,
    created_at timestamp default CURRENT_TIMESTAMP
);

alter table users
    owner to postgres;

create table kyc
(
    id            serial
        primary key,
    user_id       bigint
        unique
        references users,
    full_name     text not null,
    phone         text,
    email         text not null
        unique,
    civil_id      text
        unique,
    address       text,
    date_of_birth text,
    verified      boolean default false
);

alter table kyc
    owner to postgres;

create table accounts
(
    id             serial
        primary key,
    user_id        bigint
        references users,
    account_number text not null
        unique,
    account_type   text not null,
    balance        numeric(9, 3) default 0,
    currency       text          default 'KWD'::text,
    created_at     timestamp     default CURRENT_TIMESTAMP,
    is_active      boolean       default true
);

alter table accounts
    owner to postgres;

create table pots
(
    id               serial
        primary key,
    account_id       bigint
        references accounts,
    name             text          not null,
    balance          numeric(9, 3) default 0,
    allocation_type  text          not null,
    allocation_value numeric(9, 3) not null,
    created_at       timestamp     default CURRENT_TIMESTAMP
);

alter table pots
    owner to postgres;

create table cards
(
    id          serial
        primary key,
    account_id  bigint
        references accounts,
    pot_id      bigint
        references pots,
    card_number text,
    token       text,
    card_type   text not null,
    is_active   boolean   default true,
    created_at  timestamp default CURRENT_TIMESTAMP,
    expires_at  timestamp
);

alter table cards
    owner to postgres;

create table transactions
(
    id               serial
        primary key,
    source_id        bigint,
    amount           numeric(9, 3) not null,
    transaction_type text          not null,
    description      text,
    balance_before   numeric(9, 3) not null,
    balance_after    numeric(9, 3) not null,
    created_at       timestamp default CURRENT_TIMESTAMP,
    destination_id   bigint        not null
);

alter table transactions
    owner to postgres;

