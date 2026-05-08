# DynamoDB Database Best Practices Skill

Use this checklist when designing and operating AWS DynamoDB-backed applications.

## Scope

- Focus: table design, key modeling, access patterns, consistency, scaling, and cost control.
- Pair with Java, Quarkus, and Maven skills for implementation and runtime guidance.
- Verify business correctness before optimizing throughput or cost.
- Favor access-pattern-driven reasoning over blanket rules.

## Defaults

- Start from access patterns, then design schema.
- Prefer single-table or minimal-table design when patterns are related.
- Use on-demand capacity first unless workload is predictable.
- Enable Point-in-Time Recovery for production tables.
- Encrypt at rest and use IAM least-privilege policies.

## Working Approach

### Start With The Real Question

- Identify what items are actually needed before proposing keys, GSIs, or filters.
- Clarify which attributes are truly required on the read path.
- Determine whether the flow is transactional, operational, or analytical.
- Consider likely traffic shape, item counts, and partition-key distribution before optimizing.
- Do not optimize an access pattern that answers the wrong business question.

### Validate Logical Correctness First

- Check that the proposed key design returns the right entity set before discussing cost or speed.
- Check for item-shape or access-pattern assumptions that would force scans or post-read filtering.
- Check whether consistency choice changes the correctness of the result.
- Check whether pagination, sort-key design, and filter usage preserve the intended result semantics.
- If the access pattern is logically wrong, fix that before tuning throughput, GSIs, or retry behavior.

## Data Modeling Rules

- Design partition key for even traffic distribution.
- Use sort key to model one-to-many and time-ordered queries.
- Avoid scan-heavy workflows; prefer Query with targeted keys.
- Add GSIs only for validated read patterns.
- Keep items small; avoid storing large blobs in DynamoDB.

## Access Pattern and Query Rules

- Document each access pattern before implementation.
- Use Query with KeyConditionExpression as primary read path.
- Use FilterExpression only after key-based narrowing.
- Paginate responses using LastEvaluatedKey.
- Avoid hot partitions by spreading high-write keys.
- Prefer concrete reasoning from partition distribution, item size, consistency choice, and request frequency instead of guesswork.

## Write and Consistency Rules

- Use conditional writes for optimistic concurrency.
- Use idempotency keys for retry-safe operations.
- Use transactions only when cross-item atomicity is required.
- Use eventual consistency by default; use strong consistency only where needed.
- Apply TTL for short-lived or archival-eligible data.

## Reliability and Operations

- Monitor throttles, latency, and consumed capacity in CloudWatch.
- Configure alarms for read/write throttling and error spikes.
- Use exponential backoff with jitter on retries.
- Test with realistic load and partition-key distribution.
- Back up and rehearse restore strategy.

## Security Checklist

- Restrict IAM actions to required table/index resources.
- Use VPC endpoints for private service access where required.
- Protect PII and sensitive fields with encryption and redaction.
- Do not log full items containing secrets or personal data.
- Rotate credentials and avoid static long-lived keys.

## Cost and Performance Checklist

- Track RCUs/WCUs and item size impact.
- Project only needed attributes in GSIs.
- Use sparse indexes for selective query use cases.
- Batch reads/writes where request patterns allow.
- Review and remove unused GSIs and stale attributes.

## Review Order

1. Does the access pattern return the correct items?
2. Are the required attributes and item shapes explicit?
3. Is the partition-key and sort-key design aligned with real traffic?
4. Are scans, filters, or GSIs being used for the right reason?
5. Are consistency, retry, and transaction choices justified?
6. Are cost and hot-partition risks understood?
7. Is there a simpler table or index design that still satisfies the use case?

## Reusable Prompts

### Generate DynamoDB Design

Design a DynamoDB schema for <feature>.
Requirements:
- list explicit access patterns
- table key schema and item shapes
- required GSIs with projection choices
- read/write consistency choices
- TTL, retry, and error-handling strategy

### Review DynamoDB Usage

Review this DynamoDB implementation for:
- partition-key hot spot risks
- scan-heavy or inefficient query patterns
- concurrency and idempotency gaps
- IAM and data protection risks
- cost and scaling risks
Return findings ordered by severity with concrete fixes.
