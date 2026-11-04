INSERT INTO card_accounts (id, customer_id, credit_limit, current_balance, minimum_due, currency, status)
VALUES ('00000000-0000-0000-0000-000000000010', '858a8c45-8951-3087-a916-e4003edb1dd3', 1000.00, 100.00, 10.00, 'INR', 'ACTIVE');
INSERT INTO credit_cards (id, account_id, nickname, masked_pan, token_reference, status, frozen)
VALUES ('00000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-000000000010', 'Smoke card', 'XXXX XXXX XXXX 1234', 'SMOKE-TOKEN', 'ISSUED', false);
