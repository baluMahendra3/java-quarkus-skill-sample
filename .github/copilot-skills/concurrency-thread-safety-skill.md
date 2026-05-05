# Concurrency and Thread Safety Skill

Use this checklist for safe, predictable, and performant concurrent Java services.

## Scope

- Focus: shared-state safety, synchronization strategy, async execution, and correctness under load.
- Pair with Java, Quarkus, Testing, and Logger skills for implementation and diagnostics.

## Defaults

- Prefer stateless services and immutable data by default.
- Minimize shared mutable state across request handling paths.
- Use thread-safe collections only where contention is expected.
- Keep critical sections narrow and easy to reason about.
- Measure contention and latency before tuning.

## Thread Safety Rules

- Define ownership for mutable state clearly.
- Guard shared state with one consistent synchronization strategy.
- Use `final` for safely published dependencies and immutable fields.
- Avoid escaping partially constructed objects.
- Do not mix lock-based and lock-free approaches without clear boundaries.

## Concurrency Design Rules

- Prefer higher-level concurrency primitives over manual thread management.
- Use `CompletableFuture` and managed executors for async orchestration.
- Apply backpressure, queue limits, and timeouts for async pipelines.
- Avoid blocking operations on event-loop or request-critical threads.
- Propagate cancellation and handle interruption correctly.

## Locking and Coordination Rules

- Use intrinsic locks or `ReentrantLock` consistently per critical resource.
- Document lock ordering to avoid deadlocks.
- Avoid nested locks unless ordering is explicit and tested.
- Use read/write locks only when read-heavy patterns justify complexity.
- Keep lock scope minimal; never perform remote I/O while holding locks.

## Data Structure Rules

- Prefer immutable snapshots for read-mostly data.
- Use `ConcurrentHashMap` for concurrent key-based access patterns.
- Use atomic classes for counters/flags with simple update semantics.
- Avoid non-thread-safe collections in shared contexts.
- Consider copy-on-write only for small, infrequently updated sets.

## Failure and Observability Rules

- Log contention, timeouts, retries, and rejected tasks with correlation IDs.
- Distinguish transient concurrency failures from logical business failures.
- Add metrics for queue depth, executor saturation, and task latency.
- Fail fast on interrupted threads and restore interrupt status when needed.
- Avoid swallowing exceptions in async callbacks.

## Testing and Verification Rules

- Add stress and race-condition tests for shared-state logic.
- Validate idempotency and retry behavior under concurrent requests.
- Use deterministic tests for ordering-sensitive code where possible.
- Include timeout and cancellation scenarios in integration tests.
- Run repeated test cycles to expose flaky concurrency defects.

## Quality Gate

- Shared mutable state is minimal and explicitly guarded.
- No blocking I/O on event-loop sensitive paths.
- Executor pools, queue limits, and timeouts are configured.
- Concurrency failure paths are logged and monitored.
- Concurrency-focused tests exist for critical flows.

## Reusable Prompts

### Design Concurrent Workflow

Design concurrent execution for `<feature>`.
Requirements:
- state ownership and synchronization approach
- executor strategy, queue limits, and timeout policy
- cancellation and interruption handling
- observability plan for contention and failures
- concurrency test strategy

### Review Thread Safety

Review this implementation for:
- race conditions and memory visibility risks
- deadlock/livelock/starvation risks
- misuse of executors, futures, or synchronization
- blocking calls in sensitive threads
- missing concurrency test coverage
Return findings ordered by severity with concrete fixes.
