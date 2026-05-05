# DynamoDB Database Best Practices Skill

Use this checklist when designing and operating AWS DynamoDB-backed applications.

## Scope

- Focus: table design, key modeling, access patterns, consistency, scaling, and cost control.
- Pair with Java, Quarkus, and Maven skills for implementation and runtime guidance.

## Defaults

- Start from access patterns, then design schema.
- Prefer single-table or minimal-table design when patterns are related.
- Use on-demand capacity first unless workload is predictable.
- Enable Point-in-Time Recovery for production tables.
- Encrypt at rest and use IAM least-privilege policies.

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
