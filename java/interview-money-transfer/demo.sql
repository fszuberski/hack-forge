-- Run explicitly on a disposable local database; never reset existing balances.
INSERT INTO accounts (id, balance, currency) VALUES
    ('00000000-0000-0000-0000-000000000001', 100.00, 'USD'),
    ('00000000-0000-0000-0000-000000000002', 25.00, 'USD'),
    ('00000000-0000-0000-0000-000000000003', 50.00, 'EUR')
ON CONFLICT (id) DO NOTHING;
