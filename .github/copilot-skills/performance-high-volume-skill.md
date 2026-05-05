# Performance and High-Volume Skill

Use this checklist when designing or reviewing Java and Quarkus code for fast response times and higher request volume.

## Scope

- Focus: latency, throughput, resource efficiency, batching, backpressure, and scalable API behavior.
- Pair with Quarkus, Java, Concurrency, Maven, Logger, and Testing skills for implementation and validation details.

## Defaults

- Optimize only after identifying a real hot path or a likely scale bottleneck.
- Prefer simple designs that scale predictably over clever micro-optimizations.
- Keep request handlers short, bounded, and observable.
- Avoid loading or returning more data than the caller needs.

## API and Response Rules

- Keep response payloads compact and stable.
- Add filtering, pagination, and selective fields for list endpoints.
- Avoid returning large object graphs or persistence internals.
- Fail fast on invalid requests to avoid wasting compute.
- Use appropriate HTTP status codes and cache-friendly semantics where possible.

## Query and Data Access Rules

- Avoid N+1 query patterns and repeated lookups inside loops.
- Fetch only the fields and rows needed for the use case.
- Prefer indexed lookup paths and explicit query methods.
- Batch writes and reads where correctness allows.
- Separate read-heavy and write-heavy access patterns when needed.

## Quarkus and Runtime Rules

- Keep blocking work off event-loop sensitive paths.
- Use managed executors or reactive flows deliberately, not by default.
- Keep startup and request-scoped initialization minimal.
- Externalize pool sizes, timeouts, and cache settings via configuration.
- Use connection pools and client reuse for external systems.

## Memory and Allocation Rules

- Avoid unnecessary object creation in hot paths.
- Reuse immutable shared objects where safe.
- Be careful with large in-memory collections under high concurrency.
- Stream or page large datasets instead of materializing everything.
- Do not trade clarity for tiny allocation wins unless profiling justifies it.

## Concurrency and Backpressure Rules

- Bound queues, pools, and concurrent work.
- Add timeouts and rejection strategies for overloaded paths.
- Avoid unbounded fan-out across downstream calls.
- Prefer graceful degradation over cascading failure under load.
- Make retry behavior explicit and idempotent.

## Caching Rules

- Cache only stable or read-heavy results with clear invalidation strategy.
- Define cache key shape, TTL, and consistency tradeoffs explicitly.
- Avoid caching sensitive data unless controls are clear and justified.
- Measure cache hit rate and stale-data impact.

## Observability and Validation Rules

- Add metrics for latency, throughput, error rate, queue depth, and saturation.
- Log slow requests and downstream timeouts with correlation context.
- Benchmark critical endpoints before and after optimization.
- Use load tests for high-volume assumptions, not intuition.
- Track p50, p95, and p99 latency for important flows.

## Anti-Patterns

- Returning entire entities or nested graphs for convenience.
- Unbounded list endpoints.
- Synchronous chaining of many remote calls on request-critical paths.
- Hidden repeated database access in loops or mappers.
- Large per-request allocations or expensive serialization without need.
- Optimizing without measurement.

## Quality Gate

- High-traffic endpoints have bounded payload size and query scope.
- Hot paths avoid obvious N+1 and unbounded processing patterns.
- Timeouts, pool limits, and queue limits are configured.
- Performance-sensitive behavior has metrics and tests or benchmarks.
- Changes improve or preserve correctness while scaling more safely.

## Reusable Prompts

### Review Performance

Review this implementation for high-volume and latency risks:
- unbounded responses or processing
- N+1 queries and repeated lookups
- blocking work on sensitive paths
- memory or allocation hot spots
- missing backpressure, caching, or observability
Return findings ordered by severity with concrete fixes.

### Optimize Endpoint

Optimize this Java or Quarkus endpoint for higher volume.
Requirements:
- preserve behavior
- reduce payload size and query cost
- bound concurrency and processing
- add configuration knobs where needed
- include tests or validation steps for the optimization