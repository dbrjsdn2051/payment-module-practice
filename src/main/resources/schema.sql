CREATE TABLE payment_events
(
    id              bigint auto_increment primary key,
    buyer_id        bigint   not null,
    is_payment_done boolean  not null default false,
    payment_key     varchar(255) unique,
    order_id        varchar(255) unique,
    type            enum('NORMAL') not null,
    method          enum('EASY_PAY'),
    psp_raw_data    json,
    approved_at     datetime,
    created_at      datetime not null default current_timestamp,
    updated_at      datetime not null default current_timestamp
);

CREATE TABLE payment_orders
(
    id                   bigint auto_increment primary key,
    payment_event_id     bigint         not null,
    seller_id            bigint         not null,
    product_id           bigint         not null,
    order_id             varchar(255)   not null,
    amount               DECIMAL(12, 2) not null,
    payment_order_status ENUM('NOT_STARTED', 'EXECUTING', 'SUCCESS', 'FAILURE', 'UNKNOWN') not null default 'NOT_STARTED',
    ledger_updated       boolean        not null default false,
    wallet_updated       boolean        not null default false,
    failed_count         tinyint        not null default 0,
    threshold            tinyint        not null default 5,
    created_at           datetime       not null default current_timestamp,
    updated_at           datetime       not null default current_timestamp,

    foreign key (payment_event_id) references payment_events (id)
);

CREATE TABLE payment_order_histories
(
    id               bigint auto_increment primary key,
    payment_order_id bigint   not null,
    previous_status  ENUM('NOT_STARTED', 'EXECUTING', 'SUCCESS', 'FAILURE', 'UNKNOWN'),
    new_status       ENUM('NOT_STARTED', 'EXECUTING', 'SUCCESS', 'FAILURE', 'UNKNOWN'),
    created_at       datetime not null default current_timestamp,
    changed_by       varchar(255),
    reason           varchar(255),

    foreign key (payment_order_id) references payment_orders (id)
);






