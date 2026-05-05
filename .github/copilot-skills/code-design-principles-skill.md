# Code Design Principles Skill

Use this checklist for maintainable, modular, and evolvable Java service design.

## Scope

- Focus: SOLID-oriented design, modularity, cohesion, coupling, and API clarity.
- Pair with Architecture, Java, Quarkus, and Testing skills for implementation detail.

## Defaults

- Design around use cases and domain concepts first.
- Keep classes cohesive and small enough to explain quickly.
- Favor composition over inheritance.
- Program to interfaces where multiple implementations are expected.
- Keep dependencies explicit through constructor injection.

## Core Design Principles

- Single Responsibility: one reason to change per class/module.
- Open/Closed: extend behavior without editing stable code paths.
- Liskov Substitution: subtype behavior must honor base contracts.
- Interface Segregation: prefer small, use-case focused interfaces.
- Dependency Inversion: business policy should not depend on low-level details.

## Modularity Rules

- Organize by bounded context or feature, not by technical layer only.
- Keep public module surface small and intentional.
- Hide implementation details behind package-private classes when possible.
- Avoid cyclical dependencies between features or modules.
- Use explicit domain language in package and class names.

## API and Contract Design Rules

- Keep method contracts explicit: inputs, outputs, failure modes.
- Validate boundary inputs and fail fast with typed exceptions.
- Prefer immutable DTOs and value objects for safer flows.
- Avoid boolean parameter flags that create dual behavior.
- Version external contracts before introducing breaking changes.

## Code Smell Checklist

- God classes with unrelated responsibilities.
- Feature envy where logic belongs to another model.
- Long parameter lists and primitive obsession.
- High fan-in/fan-out without clear abstractions.
- Duplicate logic spread across modules.

## Refactoring Rules

- Refactor in small, behavior-preserving steps.
- Add or update tests before major structural changes.
- Replace condition-heavy branches with strategy/polymorphism where useful.
- Extract shared policies into domain services or value objects.
- Remove dead code and obsolete abstractions early.

## Quality Gate

- New code follows module boundaries and naming conventions.
- Public APIs have clear contracts and typed error behavior.
- No new cyclic dependencies introduced.
- Unit tests cover business behavior and edge cases.
- Design tradeoffs are documented for non-obvious decisions.

## Reusable Prompts

### Design a New Feature

Design `<feature>` using strong code design principles.
Requirements:
- module and package breakdown
- key interfaces and responsibilities
- DTO and domain model boundaries
- error and validation strategy
- test strategy for core behavior

### Review Design Quality

Review this implementation for:
- SOLID violations
- cohesion/coupling issues
- unclear contracts and abstractions
- modular boundary leaks
- refactoring priority by impact
Return findings ordered by severity with concrete fixes.
