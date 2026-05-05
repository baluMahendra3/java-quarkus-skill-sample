# Data Structures and Algorithms Skill

Use this checklist when designing or reviewing Java code where collection choice, algorithmic complexity, memory behavior, or scale-sensitive processing matters.

## Scope

- Focus: algorithmic complexity, collection choice, batching, sorting, searching, memory growth, and predictable performance under load.
- Pair with Java, Performance and High-Volume, Concurrency, Maven, and Testing skills for implementation and validation details.

## Goal

- Choose simple data structures and algorithms that fit the actual access pattern.
- Prevent accidental $O(n^2)$ or unbounded in-memory behavior in application hot paths.
- Keep code understandable while improving scalability and resource efficiency.

## Defaults

- Start with the simplest correct approach, then improve only when scale, profiling, or clear complexity risk justifies it.
- Prefer readability plus predictable complexity over clever but fragile optimizations.
- Design around expected data volume, mutation frequency, lookup shape, and ordering requirements.

## Data Structure Selection Rules

- Use `ArrayList` for append-heavy ordered collections with indexed reads.
- Use `LinkedList` rarely; prefer it only when frequent head or middle insertion and removal is proven to matter.
- Use `HashMap` for key lookups when ordering is not required.
- Use `LinkedHashMap` when insertion order matters.
- Use `TreeMap` or `TreeSet` only when sorted traversal or range operations are required.
- Use `HashSet` for fast membership checks and duplicate prevention.
- Use `EnumMap` and `EnumSet` when keys or members are enum-based.
- Use queues or deques for producer-consumer and bounded work pipelines.
- Avoid storing large request-wide working sets when streaming, paging, or batching is possible.

## Algorithm Rules

- Be explicit about time complexity for non-trivial loops, joins, merges, or searches.
- Replace repeated linear scans with indexed maps or sets when cardinality makes it worthwhile.
- Avoid nested loops over growing collections unless the upper bound is small and intentional.
- Prefer single-pass aggregation when multiple passes do not improve clarity enough to justify the cost.
- Use sorting only when the use case actually requires ordered output.
- Push filtering and aggregation to the datastore when correctness and architecture allow it.
- Bound recursion depth or prefer iterative solutions for deep or untrusted inputs.

## Scale and Memory Rules

- Avoid materializing full result sets when the caller can consume pages, streams, or batches.
- Watch for accidental object churn in mappers, collectors, and tight loops.
- Favor stable memory growth over short-lived peak allocation spikes.
- Be careful with `groupingBy`, `toMap`, and similar collectors on large datasets.
- Prefer bounded buffers, queues, and caches.

## Java Collection and Stream Rules

- Use streams when they improve clarity and do not hide repeated work or excessive allocation.
- Avoid streams for hot code paths when simple loops are clearer and cheaper.
- Avoid building intermediate collections unless they are needed by the contract.
- Be careful with `parallelStream()`; use it only with measured benefit and well-understood thread behavior.
- Prefer pre-sized collections when the approximate size is known and the path is hot.

## API and Service Design Rules

- Keep service methods explicit about whether they return all results, a page, or a summary.
- Expose filtering and pagination at boundaries instead of loading everything and trimming later.
- Separate read-model shaping from domain mutation flows.
- Keep algorithm-heavy logic inside services or dedicated utility classes, not inside resources.

## Review Questions

- What is the expected cardinality of the input and output?
- What is the dominant operation: lookup, insertion, removal, traversal, grouping, or sorting?
- Does the code repeat work inside loops that can be precomputed once?
- Would a map, set, or pre-index reduce complexity meaningfully?
- Is this path request-critical, batch-oriented, or background work?
- Can the work be bounded, paged, or delegated to storage?

## Anti-Patterns

- Nested scans across two large collections without indexing.
- Loading all rows and filtering in memory for convenience.
- Returning full entity graphs where a summary or DTO would do.
- Using `List.contains()` repeatedly where `Set` membership is required.
- Sorting repeatedly inside loops.
- Using recursion where input depth is unbounded.
- Using `parallelStream()` as a default optimization.

## Quality Gate

- Complexity risks are understood for hot or high-volume paths.
- Collection choice matches access patterns and ordering needs.
- Large results are paged, streamed, or batched where appropriate.
- Algorithm-heavy behavior has tests for correctness and edge cases.
- Performance-sensitive changes are supported by measurement or a clear complexity argument.

## Reusable Prompts

### Review For Algorithmic Scale

Review this Java implementation for data-structure and algorithm issues:
- wrong collection choices
- accidental $O(n^2)$ work
- repeated scans and missed indexing
- unbounded memory growth
- unnecessary sorting or copying
Return findings ordered by severity with concrete fixes.

### Refactor For Better Complexity

Refactor this Java code to improve scale safely.
Requirements:
- preserve behavior
- improve collection and algorithm choice
- bound memory where possible
- explain complexity before and after
- add or update tests for edge cases