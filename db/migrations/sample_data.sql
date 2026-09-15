BEGIN;

-- ============================================================
-- 1. ADVISOR - 10 rows
-- ============================================================

INSERT INTO advisor
(
    advisor_id,
    advisor_name,
    email,
    password_hash,
    hire_date
)
VALUES
(1, 'Sarah Johnson',
 'sarah.johnson@broker.example',
 '$2b$12$examplehash001',
 '2018-05-14'),

(2, 'Michael Brown',
 'michael.brown@broker.example',
 '$2b$12$examplehash002',
 '2020-03-09'),

(3, 'Emma Wilson',
 'emma.wilson@broker.example',
 '$2b$12$examplehash003',
 '2021-11-22'),

(4, 'Daniel Murphy',
 'daniel.murphy@broker.example',
 '$2b$12$examplehash004',
 '2019-02-11'),

(5, 'Emily Davis',
 'emily.davis@broker.example',
 '$2b$12$examplehash005',
 '2019-09-23'),

(6, 'Robert Miller',
 'robert.miller@broker.example',
 '$2b$12$examplehash006',
 '2020-07-06'),

(7, 'Grace Wilson',
 'grace.wilson@broker.example',
 '$2b$12$examplehash007',
 '2021-01-18'),

(8, 'Thomas Moore',
 'thomas.moore@broker.example',
 '$2b$12$examplehash008',
 '2021-05-31'),

(9, 'Charlotte Clark',
 'charlotte.clark@broker.example',
 '$2b$12$examplehash009',
 '2022-09-12'),

(10, 'William Lewis',
 'william.lewis@broker.example',
 '$2b$12$examplehash010',
 '2023-03-20');


-- ============================================================
-- 2. CLIENT - 10 rows
-- ============================================================

INSERT INTO client
(
    client_id,
    client_name,
    email,
    password_hash,
    phone_no,
    dob,
    join_date,
    risk_profile,
    advisor_id
)
VALUES
(101, 'John Smith',
 'john.smith@example.com',
 '$2b$12$clienthash001',
 '+353850000001',
 '1985-04-12',
 '2022-01-10',
 'MODERATE',
 1),

(102, 'Olivia Martin',
 'olivia.martin@example.com',
 '$2b$12$clienthash002',
 '+353850000002',
 '1990-08-21',
 '2022-04-15',
 'AGGRESSIVE',
 1),

(103, 'David Taylor',
 'david.taylor@example.com',
 '$2b$12$clienthash003',
 '+353850000003',
 '1978-02-18',
 '2023-02-01',
 'CONSERVATIVE',
 2),

(104, 'Sophia Anderson',
 'sophia.anderson@example.com',
 '$2b$12$clienthash004',
 '+353850000004',
 '1995-10-03',
 '2023-06-12',
 'MODERATE',
 3),

(105, 'James Thomas',
 'james.thomas@example.com',
 '$2b$12$clienthash005',
 '+353850000005',
 '1982-12-29',
 '2024-01-20',
 'AGGRESSIVE',
 2),

(106, 'Liam Harris',
 'liam.harris@example.com',
 '$2b$12$clienthash006',
 '+353850000006',
 '1988-03-15',
 '2024-02-12',
 'MODERATE',
 4),

(107, 'Amelia Walker',
 'amelia.walker@example.com',
 '$2b$12$clienthash007',
 '+353850000007',
 '1992-11-07',
 '2024-05-08',
 'AGGRESSIVE',
 5),

(108, 'Noah Hall',
 'noah.hall@example.com',
 '$2b$12$clienthash008',
 '+353850000008',
 '1975-06-25',
 '2024-07-19',
 'CONSERVATIVE',
 6),

(109, 'Mia Allen',
 'mia.allen@example.com',
 '$2b$12$clienthash009',
 '+353850000009',
 '1998-01-30',
 '2025-01-15',
 'MODERATE',
 7),

(110, 'Oliver Young',
 'oliver.young@example.com',
 '$2b$12$clienthash010',
 '+353850000010',
 '1986-09-17',
 '2025-04-21',
 'AGGRESSIVE',
 8);


-- ============================================================
-- 3. ACCOUNT - 10 rows
-- ============================================================

INSERT INTO account
(
    account_id,
    account_number,
    opening_date,
    balance,
    purchasing_power,
    account_status,
    currency,
    version,
    suspended_at,
    closed_at,
    client_id
)
VALUES
(
    1001,
    'ACC000001001',
    '2022-01-10',
    25000.0000,
    18000.0000,
    'ACTIVE',
    'EUR',
    7,
    NULL,
    NULL,
    101
),
(
    1002,
    'ACC000001002',
    '2022-04-15',
    15000.0000,
    12000.0000,
    'SUSPENDED',
    'EUR',
    3,
    '2026-08-20 09:30:00+01',
    NULL,
    102
),
(
    1003,
    'ACC000001003',
    '2023-02-01',
    0.0000,
    0.0000,
    'CLOSED',
    'GBP',
    12,
    NULL,
    '2026-06-30 16:15:00+01',
    103
),
(
    1004,
    'ACC000001004',
    '2023-06-12',
    42000.0000,
    35000.0000,
    'ACTIVE',
    'USD',
    5,
    NULL,
    NULL,
    104
),
(
    1005,
    'ACC000001005',
    '2024-02-12',
    18000.0000,
    15000.0000,
    'ACTIVE',
    'EUR',
    4,
    NULL,
    NULL,
    105
),
(
    1006,
    'ACC000001006',
    '2024-05-08',
    32500.0000,
    25000.0000,
    'ACTIVE',
    'USD',
    6,
    NULL,
    NULL,
    106
),
(
    1007,
    'ACC000001007',
    '2024-07-19',
    9500.0000,
    7000.0000,
    'SUSPENDED',
    'GBP',
    8,
    '2026-08-18 10:20:00+01',
    NULL,
    107
),
(
    1008,
    'ACC000001008',
    '2025-01-15',
    0.0000,
    0.0000,
    'CLOSED',
    'EUR',
    11,
    NULL,
    '2026-05-14 15:30:00+01',
    108
),
(
    1009,
    'ACC000001009',
    '2025-04-21',
    27500.0000,
    21000.0000,
    'ACTIVE',
    'EUR',
    3,
    NULL,
    NULL,
    109
),
(
    1010,
    'ACC000001010',
    '2025-07-10',
    61500.0000,
    50000.0000,
    'ACTIVE',
    'USD',
    9,
    NULL,
    NULL,
    110
);


-- ============================================================
-- 4. INSTRUMENT - 13 rows
-- ============================================================

INSERT INTO instrument
(
    instrument_id,
    instrument_ticker,
    instrument_name,
    asset_class,
    instrument_status
)
VALUES
(2001,
 'AAPL',
 'Apple Inc.',
 'EQUITY',
 'TRADING'),

(2002,
 'MSFT',
 'Microsoft Corporation',
 'EQUITY',
 'TRADING'),

(2003,
 'VOD',
 'Vodafone Group Plc',
 'EQUITY',
 'HALTED'),

(2004,
 'GOOGL',
 'Alphabet Inc.',
 'EQUITY',
 'TRADING'),

(2005,
 'AMZN',
 'Amazon.com Inc.',
 'EQUITY',
 'TRADING'),

(2006,
 'TSLA',
 'Tesla Inc.',
 'EQUITY',
 'TRADING'),

(2007,
 'META',
 'Meta Platforms Inc.',
 'EQUITY',
 'TRADING'),

(2008,
 'IBM',
 'International Business Machines',
 'EQUITY',
 'HALTED'),

(2009,
 'SPY',
 'SPDR S&P 500 ETF Trust',
 'ETF',
 'TRADING'),

(2010,
 'OLD1',
 'Retired Investment Fund',
 'FUND',
 'RETIRED'),

(2011,
 'INFY.NS',
 'Infosys Ltd',
 'EQUITY',
 'TRADING'),

(2012,
 'RELIANCE.NS',
 'Reliance Industries Ltd',
 'EQUITY',
 'TRADING'),

(2013,
 'TATASTEEL.BO',
 'Tata Steel Ltd',
 'EQUITY',
 'TRADING');


-- ============================================================
-- 5. ORDERS - 10 rows
-- ============================================================
-- Allowed statuses:
-- NEW
-- FILLED
-- CANCELLED
-- REJECTED
--
-- No RECEIVED state.
-- No HALF_FILLED state.
-- ============================================================

INSERT INTO orders
(
    order_id,
    idempotency_key,
    order_status,
    received_at,
    order_type,
    price,
    quantity,
    transaction_date,
    account_id,
    instrument_id
)
VALUES
(
    3001,
    'IDEMP-20260823-0001',
    'NEW',
    '2026-08-23 09:00:00+01',
    'BUY',
    185.2500,
    10,
    NULL,
    1001,
    2001
),
(
    3002,
    'IDEMP-20260823-0002',
    'FILLED',
    '2026-08-22 10:15:00+01',
    'BUY',
    520.7500,
    5,
    '2026-08-22 10:16:32+01',
    1004,
    2002
),
(
    3003,
    'IDEMP-20260823-0003',
    'CANCELLED',
    '2026-08-21 11:00:00+01',
    'SELL',
    190.0000,
    20,
    '2026-08-21 11:05:10+01',
    1001,
    2001
),
(
    3004,
    'IDEMP-20260823-0004',
    'REJECTED',
    '2026-08-20 14:20:00+01',
    'SELL',
    8.5000,
    100,
    '2026-08-20 14:20:01+01',
    1002,
    2003
),
(
    3005,
    'IDEMP-20260823-0005',
    'NEW',
    '2026-08-23 08:30:00+01',
    'BUY',
    195.5000,
    15,
    NULL,
    1005,
    2004
),
(
    3006,
    'IDEMP-20260823-0006',
    'FILLED',
    '2026-08-22 09:45:00+01',
    'SELL',
    135.2500,
    20,
    '2026-08-22 09:47:20+01',
    1006,
    2005
),
(
    3007,
    'IDEMP-20260823-0007',
    'CANCELLED',
    '2026-08-21 12:10:00+01',
    'BUY',
    250.7500,
    12,
    '2026-08-21 12:30:00+01',
    1009,
    2006
),
(
    3008,
    'IDEMP-20260823-0008',
    'REJECTED',
    '2026-08-20 13:15:00+01',
    'BUY',
    505.0000,
    8,
    '2026-08-20 13:16:05+01',
    1007,
    2008
),
(
    3009,
    'IDEMP-20260823-0009',
    'FILLED',
    '2026-08-19 14:00:00+01',
    'BUY',
    585.3000,
    30,
    '2026-08-19 14:00:45+01',
    1010,
    2009
),
(
    3010,
    'IDEMP-20260823-0010',
    'REJECTED',
    '2026-08-18 15:25:00+01',
    'SELL',
    75.0000,
    50,
    '2026-08-18 15:26:00+01',
    1008,
    2010
);


-- ============================================================
-- 6. HOLDINGS - 10 rows
-- ============================================================

INSERT INTO holdings
(
    holding_id,
    quantity,
    purchase_price,
    account_id,
    instrument_id
)
VALUES
(
    4001,
    50,
    175.5000,
    1001,
    2001
),
(
    4002,
    25,
    142.7500,
    1005,
    2004
),
(
    4003,
    40,
    128.5000,
    1006,
    2005
),
(
    4004,
    15,
    240.2500,
    1009,
    2006
),
(
    4005,
    60,
    485.7500,
    1010,
    2007
),
(
    4006,
    30,
    172.3500,
    1005,
    2009
),
(
    4007,
    20,
    450.0000,
    1006,
    2002
),
(
    4008,
    75,
    178.2500,
    1009,
    2001
),
(
    4009,
    10,
    120.0000,
    1007,
    2008
),
(
    4010,
    100,
    50.0000,
    1010,
    2010
);


COMMIT;
