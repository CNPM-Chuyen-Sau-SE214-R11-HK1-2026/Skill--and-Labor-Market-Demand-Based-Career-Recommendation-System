import { apiClient, resetUnauthorizedHandling } from '../src/services/api';
import { tokenStorage } from '../src/utils/tokenStorage';
import { AxiosError, AxiosResponse, InternalAxiosRequestConfig } from 'axios';

// Mock localStorage in Node environment
const store: Record<string, string> = {};
globalThis.localStorage = {
  getItem: (key: string) => store[key] ?? null,
  setItem: (key: string, val: string) => {
    store[key] = String(val);
  },
  removeItem: (key: string) => {
    delete store[key];
  },
  clear: () => {
    for (const k in store) delete store[k];
  },
  length: 0,
  key: () => null,
};

// Mock window and Event in Node environment
let dispatchedEvents: string[] = [];
globalThis.window = {
  dispatchEvent: (evt: Event) => {
    dispatchedEvents.push(evt.type);
    return true;
  },
} as unknown as Window & typeof globalThis;

globalThis.Event = class Event {
  type: string;
  constructor(type: string) {
    this.type = type;
  }
} as unknown as typeof Event;

interface TestResult {
  scenario: string;
  passed: boolean;
  details: Record<string, unknown>;
}

async function runVerification() {
  const results: TestResult[] = [];

  // =========================================================================
  // SCENARIO A: Race Condition
  // R1 sent with T1 -> re-login with T2 -> R1 returns 401 late
  // Expected: T2 preserved, 0 auth:unauthorized events
  // =========================================================================
  store['token'] = 'T1';
  store['user'] = JSON.stringify({ id: 1, email: 'user@test.com' });
  dispatchedEvents = [];
  resetUnauthorizedHandling();

  let resolveR1: () => void = () => {};
  const r1HoldPromise = new Promise<void>((res) => {
    resolveR1 = res;
  });

  apiClient.defaults.adapter = async (config: InternalAxiosRequestConfig) => {
    if (config.url === '/slow-request') {
      await r1HoldPromise;
      const err = new AxiosError(
        'Request failed with status code 401',
        'ERR_BAD_REQUEST',
        config,
        null,
        {
          status: 401,
          statusText: 'Unauthorized',
          headers: {},
          config,
          data: { message: 'Unauthorized' },
        } as AxiosResponse
      );
      throw err;
    }
    return {
      data: { success: true },
      status: 200,
      statusText: 'OK',
      headers: {},
      config,
    };
  };

  // Launch R1 (slow request with T1)
  const r1Promise = apiClient.get('/slow-request').catch((err) => err);

  // Wait a tick for Axios async request interceptor to attach T1 to R1 before user re-logs in
  await new Promise((r) => setTimeout(r, 10));

  // User re-authenticates with T2 while R1 is in flight
  tokenStorage.saveSession({
    accessToken: 'T2',
    tokenType: 'Bearer',
    expiresIn: 86400,
    user: { id: 1, email: 'user@test.com', fullName: 'User', role: 'ROLE_CANDIDATE' },
  });
  resetUnauthorizedHandling();

  // Now let R1 return 401
  resolveR1();
  await r1Promise;

  const scenarioAPassed =
    tokenStorage.getToken() === 'T2' && dispatchedEvents.length === 0;

  results.push({
    scenario: 'Scenario A: Race condition guard (Late 401 with old token T1)',
    passed: scenarioAPassed,
    details: {
      currentTokenInStorage: tokenStorage.getToken(),
      dispatchedEventsCount: dispatchedEvents.length,
      expectedToken: 'T2',
    },
  });

  // =========================================================================
  // SCENARIO B: Current Session 401
  // Request sent with current token T1 returns 401
  // Expected: Session cleared, exactly 1 auth:unauthorized event
  // =========================================================================
  tokenStorage.saveSession({
    accessToken: 'T1',
    tokenType: 'Bearer',
    expiresIn: 86400,
    user: { id: 1, email: 'user@test.com', fullName: 'User', role: 'ROLE_CANDIDATE' },
  });
  dispatchedEvents = [];
  resetUnauthorizedHandling();

  apiClient.defaults.adapter = async (config: InternalAxiosRequestConfig) => {
    const err = new AxiosError(
      'Unauthorized',
      'ERR_BAD_REQUEST',
      config,
      null,
      {
        status: 401,
        statusText: 'Unauthorized',
        headers: {},
        config,
        data: {},
      } as AxiosResponse
    );
    throw err;
  };

  await apiClient.get('/candidate/profile').catch(() => {});

  const scenarioBPassed =
    tokenStorage.getToken() === null &&
    dispatchedEvents.length === 1 &&
    dispatchedEvents[0] === 'auth:unauthorized';

  results.push({
    scenario: 'Scenario B: Current session receives 401',
    passed: scenarioBPassed,
    details: {
      currentTokenInStorage: tokenStorage.getToken(),
      dispatchedEvents,
    },
  });

  // =========================================================================
  // SCENARIO C: Multiple Concurrent 401s
  // 5 requests sent in parallel with current token T1 all return 401
  // Expected: Session cleared once, exactly 1 auth:unauthorized event
  // =========================================================================
  tokenStorage.saveSession({
    accessToken: 'T1',
    tokenType: 'Bearer',
    expiresIn: 86400,
    user: { id: 1, email: 'user@test.com', fullName: 'User', role: 'ROLE_CANDIDATE' },
  });
  dispatchedEvents = [];
  resetUnauthorizedHandling();

  apiClient.defaults.adapter = async (config: InternalAxiosRequestConfig) => {
    // Artificial small delay to interleave parallel execution
    await new Promise((r) => setTimeout(r, 10));
    const err = new AxiosError(
      'Unauthorized',
      'ERR_BAD_REQUEST',
      config,
      null,
      {
        status: 401,
        statusText: 'Unauthorized',
        headers: {},
        config,
        data: {},
      } as AxiosResponse
    );
    throw err;
  };

  await Promise.all([
    apiClient.get('/res-1').catch(() => {}),
    apiClient.get('/res-2').catch(() => {}),
    apiClient.get('/res-3').catch(() => {}),
    apiClient.get('/res-4').catch(() => {}),
    apiClient.get('/res-5').catch(() => {}),
  ]);

  const scenarioCPassed =
    tokenStorage.getToken() === null && dispatchedEvents.length === 1;

  results.push({
    scenario: 'Scenario C: 5 concurrent 401 responses',
    passed: scenarioCPassed,
    details: {
      currentTokenInStorage: tokenStorage.getToken(),
      dispatchedEventsCount: dispatchedEvents.length,
    },
  });

  // =========================================================================
  // SCENARIO D: Mock Token Isolation & Hardening
  // D1: Storage contains mock token -> Real request strips Authorization
  // D2: Caller explicitly passed mock token -> Real request strips Authorization
  // =========================================================================
  tokenStorage.saveSession({
    accessToken: 'mock-token-abc-999',
    tokenType: 'Bearer',
    expiresIn: 86400,
    user: { id: 99, email: 'mock@test.com', fullName: 'Mock User', role: 'ROLE_CANDIDATE' },
  });

  let capturedAuthHeaderD1: unknown = 'NOT_CALLED';
  let capturedAuthHeaderD2: unknown = 'NOT_CALLED';

  apiClient.defaults.adapter = async (config: InternalAxiosRequestConfig) => {
    if (config.url === '/test-d1') {
      capturedAuthHeaderD1 = config.headers?.get?.('Authorization') ?? config.headers?.Authorization;
    } else if (config.url === '/test-d2') {
      capturedAuthHeaderD2 = config.headers?.get?.('Authorization') ?? config.headers?.Authorization;
    }
    return {
      data: { success: true },
      status: 200,
      statusText: 'OK',
      headers: {},
      config,
    };
  };

  // D1: Normal request with mock token in storage
  await apiClient.get('/test-d1');

  // D2: Request where caller explicitly provided mock token header
  await apiClient.get('/test-d2', {
    headers: { Authorization: 'Bearer mock-token-caller-leak' } as unknown as undefined,
  });

  const scenarioDPassed =
    capturedAuthHeaderD1 === undefined && capturedAuthHeaderD2 === undefined;

  results.push({
    scenario: 'Scenario D: Mock token isolation & hardening (Storage & Caller config)',
    passed: scenarioDPassed,
    details: {
      capturedAuthHeaderD1,
      capturedAuthHeaderD2,
      expectedBoth: 'undefined',
    },
  });

  // =========================================================================
  // SCENARIO E: Real Valid JWT Token Attachment
  // Storage contains real valid JWT -> Request receives Authorization: Bearer <token>
  // =========================================================================
  tokenStorage.saveSession({
    accessToken: 'real.jwt.token.payload.signature',
    tokenType: 'Bearer',
    expiresIn: 86400,
    user: { id: 1, email: 'real@test.com', fullName: 'Real User', role: 'ROLE_CANDIDATE' },
  });

  let capturedAuthHeaderE: unknown = 'NOT_CALLED';

  apiClient.defaults.adapter = async (config: InternalAxiosRequestConfig) => {
    capturedAuthHeaderE = config.headers?.get?.('Authorization') ?? config.headers?.Authorization;
    return {
      data: { success: true },
      status: 200,
      statusText: 'OK',
      headers: {},
      config,
    };
  };

  await apiClient.get('/occupations');

  const scenarioEPassed =
    capturedAuthHeaderE === 'Bearer real.jwt.token.payload.signature';

  results.push({
    scenario: 'Scenario E: Real valid JWT attached as Bearer',
    passed: scenarioEPassed,
    details: {
      capturedAuthHeaderE,
      expected: 'Bearer real.jwt.token.payload.signature',
    },
  });

  // =========================================================================
  // SCENARIO F: Login 401 skips global unauthorized handling
  // POST /auth/login returns 401 -> No event dispatched, error thrown to caller
  // =========================================================================
  tokenStorage.clearSession();
  dispatchedEvents = [];
  resetUnauthorizedHandling();

  apiClient.defaults.adapter = async (config: InternalAxiosRequestConfig) => {
    const err = new AxiosError(
      'Invalid credentials',
      'ERR_BAD_REQUEST',
      config,
      null,
      {
        status: 401,
        statusText: 'Unauthorized',
        headers: {},
        config,
        data: { message: 'Bad credentials' },
      } as AxiosResponse
    );
    throw err;
  };

  let callerCaughtError = false;
  try {
    await apiClient.post('/auth/login', { email: 'bad@test.com', password: 'wrong' });
  } catch {
    callerCaughtError = true;
  }

  const scenarioFPassed =
    callerCaughtError && dispatchedEvents.length === 0;

  results.push({
    scenario: 'Scenario F: /auth/login 401 skips global unauthorized handling',
    passed: scenarioFPassed,
    details: {
      callerCaughtError,
      dispatchedEventsCount: dispatchedEvents.length,
    },
  });

  // Print Summary
  console.log('=== PRODUCTION MODULE VERIFICATION RESULTS ===');
  console.log(JSON.stringify(results, null, 2));

  const allPassed = results.every((r) => r.passed);
  console.log(`\nOVERALL VERDICT: ${allPassed ? 'ALL SCENARIOS PASSED' : 'SOME SCENARIOS FAILED'}`);
  if (!allPassed) {
    process.exit(1);
  }
}

runVerification().catch((err) => {
  console.error('Test execution failed:', err);
  process.exit(1);
});
