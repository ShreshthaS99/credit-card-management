INSERT INTO statements (id, account_id, cycle_start, cycle_end, opening_balance, closing_balance, minimum_due, due_date, status, version)
VALUES ('00000000-0000-0000-0000-000000000013', '00000000-0000-0000-0000-000000000010', '2026-10-01', '2026-10-31', 0.00, 100.00, 10.00, '2026-11-20', 'PUBLISHED', 1);
INSERT INTO statement_lines (id, statement_id, description, amount, type)
VALUES ('00000000-0000-0000-0000-000000000014', '00000000-0000-0000-0000-000000000013', 'Smoke purchase', 100.00, 'PURCHASE');
