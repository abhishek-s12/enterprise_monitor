export const environment = {
  production: false,
  // ✅ FIX 3: API key injected via Angular interceptor into every HTTP request.
  // In development this matches the API_KEY value in your root .env file.
  apiKey: 'sla-monitor-api-key-dev-only-CHANGE_ME'
};
