# Lombok Skill

Use this checklist when adding or maintaining Lombok in Java code.

## Scope

- Focus: safe Lombok usage, readability, framework compatibility, and minimizing boilerplate without obscuring behavior.
- Pair with Java, Quarkus, Architecture, and Testing skills for broader implementation guidance.

## Defaults

- Use Lombok only when it improves clarity and reduces repetitive boilerplate.
- Prefer explicit code when behavior, lifecycle, or invariants would be hidden by annotations.
- Keep generated methods predictable and aligned with the domain model.

## Annotation Selection Rules

- Prefer `@Getter` and `@Setter` over `@Data` for mutable entities and framework-managed models.
- Prefer `@Value` for immutable DTOs and value objects when immutability is desired.
- Use `@Builder` for complex construction flows, optional fields, or test fixture readability.
- Use `@NoArgsConstructor` and `@AllArgsConstructor` only when frameworks or mapping layers require them.
- Use `@RequiredArgsConstructor` for constructor injection when the class design is otherwise simple and clear.

## Entity and Persistence Rules

- Be cautious using Lombok on JPA entities.
- Do not use `@Data` on entities because generated `equals`, `hashCode`, and `toString` can break persistence expectations.
- Avoid `@EqualsAndHashCode` on entities unless identity semantics are explicitly designed.
- Avoid `@ToString` on entities with relationships that may trigger recursion or lazy-loading side effects.
- Keep lifecycle methods and persistence-related defaults explicit in entity classes.

## API and DTO Rules

- Lombok is acceptable for DTOs when it keeps the boundary concise and readable.
- Prefer immutable DTOs where practical.
- Ensure generated constructors and accessors match serializer and deserializer needs.
- Do not let Lombok hide validation or contract-critical behavior.

## Readability Rules

- Do not stack Lombok annotations mechanically.
- Keep the chosen annotation set minimal and intention-revealing.
- If Lombok makes the class harder to understand, write the methods explicitly instead.
- Make non-obvious defaults, invariants, and side effects explicit in code.

## Testing and Quality Gate

- Verify serialization and deserialization behavior for Lombok-backed DTOs.
- Test domain behavior, not generated getters and setters.
- Confirm framework integration still works for CDI, JPA, and Jackson use cases.
- Avoid introducing Lombok patterns that make debugging or stack traces harder to follow.

## Anti-Patterns

- Using `@Data` on JPA entities.
- Using Lombok to hide business logic, validation, or state transitions.
- Combining too many annotations so object construction becomes unclear.
- Relying on generated methods without considering framework requirements.

## Reusable Prompts

### Review Lombok Usage

Review this Java code for Lombok usage:
- inappropriate annotations for entities or DTOs
- hidden framework compatibility risks
- readability and maintainability concerns
- better explicit-code alternatives where needed
Return findings ordered by severity with concrete fixes.

### Refactor with Lombok

Refactor this Java class to use Lombok where appropriate.
Requirements:
- preserve behavior
- avoid risky Lombok usage on entities
- keep framework compatibility intact
- keep constructors and API contracts explicit where needed
- update tests if serialization or construction changes