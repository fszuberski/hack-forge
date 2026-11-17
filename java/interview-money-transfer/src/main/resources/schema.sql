CREATE TABLE IF NOT EXISTS accounts (
    id uuid PRIMARY KEY,
    balance numeric NOT NULL,
    currency varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS transfers (
    id uuid PRIMARY KEY,
    source_account_id uuid NOT NULL REFERENCES accounts(id),
    target_account_id uuid NOT NULL REFERENCES accounts(id),
    amount numeric NOT NULL,
    currency varchar(255) NOT NULL,
    idempotency_key varchar(255),
    source_balance numeric NOT NULL,
    target_balance numeric NOT NULL
);

-- Empty seam for the candidate: production code does not write ledger postings yet.
-- Signed amount: source debit negative, target credit positive.
CREATE TABLE IF NOT EXISTS ledger_entries (
    id uuid PRIMARY KEY,
    transaction_id uuid NOT NULL REFERENCES transfers(id),
    account_id uuid NOT NULL REFERENCES accounts(id),
    amount numeric NOT NULL,
    currency varchar(255) NOT NULL,
    balance_after numeric NOT NULL
);
