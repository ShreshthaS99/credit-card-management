import { http } from "./http";

export async function getCustomer(customerId) {
  const response = await http.get(
    `/api/customers/${customerId}`
  );

  return response.data;
}

export async function updateCustomer(
  customerId,
  customer
) {
  const response = await http.put(
    `/api/customers/${customerId}`,
    customer
  );

  return response.data;
}

export async function getCustomerAccounts(customerId) {
  const response = await http.get(
    `/api/accounts/customer/${customerId}`
  );

  return response.data;
}

export async function getAccount(accountId) {
  const response = await http.get(
    `/api/accounts/${accountId}`
  );

  return response.data;
}

export async function getCards(accountId) {
  const response = await http.get(
    "/api/cards",
    {
      params: { accountId }
    }
  );

  return response.data;
}

export async function activateCard(cardId) {
  const response = await http.post(
    `/api/cards/${cardId}/activate`
  );

  return response.data;
}

export async function freezeCard(cardId) {
  const response = await http.post(
    `/api/cards/${cardId}/freeze`
  );

  return response.data;
}

export async function unfreezeCard(cardId) {
  const response = await http.post(
    `/api/cards/${cardId}/unfreeze`
  );

  return response.data;
}
export async function getTransactions(
  accountId,
  page = 0,
  size = 20
) {
  const response = await http.get(
    "/api/transactions",
    {
      params: {
        accountId,
        page,
        size
      }
    }
  );

  return response.data;
}

export async function createPayment(
  payment,
  idempotencyKey
) {
  const response = await http.post(
    "/api/payments",
    payment,
    {
      headers: {
        "Idempotency-Key": idempotencyKey
      }
    }
  );

  return response.data;
}

export async function getPayments(accountId) {
  const response = await http.get(
    "/api/payments",
    {
      params: { accountId }
    }
  );

  return response.data;
}

export async function getStatements(accountId) {
  const response = await http.get(
    "/api/statements",
    {
      params: { accountId }
    }
  );

  return response.data;
}

export async function getStatementLines(statementId) {
  const response = await http.get(
    `/api/statements/${statementId}/lines`
  );

  return response.data;
}

export async function createLimitRequest(request) {
  const response = await http.post(
    "/api/limits/requests",
    request
  );

  return response.data;
}

export async function getLimitRequests(accountId) {
  const response = await http.get(
    "/api/limits/requests",
    {
      params: { accountId }
    }
  );

  return response.data;
}

export async function getRewardBalance(accountId) {
  const response = await http.get(
    "/api/rewards/balance",
    {
      params: { accountId }
    }
  );

  return response.data;
}

export async function getRewardLedger(accountId) {
  const response = await http.get(
    "/api/rewards/ledger",
    {
      params: { accountId }
    }
  );

  return response.data;
}

export async function redeemRewards(request) {
  const response = await http.post(
    "/api/rewards/redemptions",
    request
  );

  return response.data;
}

export async function getFraudAlerts(accountId) {
  const response = await http.get(
    "/api/fraud/alerts",
    {
      params: { accountId }
    }
  );

  return response.data;
}

export async function respondToFraudAlert(
  alertId,
  customerResponse
) {
  const response = await http.post(
    `/api/fraud/alerts/${alertId}/response`,
    {
      response: customerResponse
    }
  );

  return response.data;
}