export const environment = {
  production: true,
  // ✅ FIX 3: In production builds, this value is replaced at build time.
  // To override without rebuilding: inject via nginx env vars or a runtime config endpoint.
  apiKey: 'sla-monitor-api-key-dev-only-CHANGE_ME'
};
