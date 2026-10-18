# Network Foundation (Phase 3.1)

`app -> core:network` only. No dependencies from domain/data/database to network yet,
no core -> app imports, and no repositories or business endpoints.
Hilt bindings live in the app composition root; network types remain transport-only.

## Configuration

`NetworkModule.config()` supplies `NetworkConfig()` (disabled) for every existing build type.
No development, staging or production API URL has been approved in the available TECH-04 baseline.
The reserved `https://unconfigured.invalid/api/v1/` Retrofit placeholder is blocked by the
application interceptor before DNS/socket access. It is not an environment URL.
When an approved environment is available, replace that provider with
`NetworkConfig(approvedApiBaseUrl.toHttpUrl())`; include the approved API path and trailing slash.
No new flavors, secrets, build parameters or deployment policy are introduced.
Only HTTPS URLs without user info, query or fragment are accepted; requests must stay
within the configured origin and path. Local tests trust an ephemeral localhost certificate;
production TLS verification is unchanged.

## Contracts and policy

TECH-01 §3.1 allows choosing a serializer at implementation: Kotlin Serialization is
selected here with the official Retrofit converter. TECH-03 §2.1/§6.3 supply generic
single/list envelopes and optional unified error details. These are candidate wire
contracts until backend/OpenAPI review; no business fields or API services are added.

`networkRequest` distinguishes HTTP status/error metadata (including unknown codes),
connection failure, timeout, invalid JSON and disabled configuration. Cancellation propagates.
204 returns success with a null body. Callers must handle nullable bodies explicitly.
Malformed, missing or oversized error payloads retain HTTP status with no parsed error.
Error parsing is capped at 64 KiB, a local defensive limit, not a backend payload contract.
Server error text is untrusted transport data, not UI copy. No body/token logging is installed.

Connection recovery and redirects are disabled. Mutation bodies are one-shot, also
preventing OkHttp's HTTP 503 immediate follow-up from replaying a write. There is no
application retry, authenticator, token refresh, replay queue, offline success or cache.
OkHttp can still follow its built-in status retry rules for bodyless reads; this is not
an application retry scheduler. `retryable` and raw `Retry-After` are advisory metadata,
never permission to replay a mutation. OkHttp's existing 10-second connect/read/write
defaults remain library defaults, not approved environment/SLA settings. Total call
budgets and endpoint retry/idempotency policies require later contract review.

Tests use test-only fixture routes over HTTPS, never a deployed backend. They cover
base URL validation, origin/path containment, disabled configuration, JSON envelopes,
204, invalid JSON, error fallbacks, HTTP status families, Retry-After, cancellation,
transport failures and prevention of mutation replay/redirect.
