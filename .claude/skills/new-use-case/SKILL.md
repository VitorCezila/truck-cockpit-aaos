---
name: new-use-case
description: Create a pure-Kotlin use case in :core:domain (one operation, e.g. EstimateRangeUseCase, CheckDestinationReachableUseCase, energy per km) with JUnit/Turbine tests. Use when asked to add a calculation, rule or business decision that screens consume.
---

# New use case

## When to use
- A calculation or rule: energy use per km, range estimate, "will the battery reach the destination".
- Logic currently sitting inside a ViewModel that should be reusable or tested in isolation.

## Step by step
1. **Name it after the operation:** `<Verb><Noun>UseCase` in `com.cezila.truckcockpit.core.domain.usecase`. One public `operator fun invoke(...)` (or `suspend`/returning `Flow` if it observes data). One use case = one operation; split if you find an "and" in the name.
2. **Inputs and outputs are domain types** (`VehicleState`, value classes for units such as `Kilometers`, `KilowattHours`). No Android, no `android.car`, no Hilt annotations other than `@Inject constructor` (javax.inject is allowed in `:core:domain`).
3. **Model "can't compute".** If an input is unavailable (no battery level, no capacity), return a result type that says so (`sealed interface` result or nullable), never a fake 0.
4. **Edge cases first.** Write down: zero/negative distance, speed 0, full/empty battery, division by zero for per-km values, values out of range.
5. **Tests** in `core/domain/src/test` (JUnit; Turbine if the use case returns a `Flow`):
   - Happy path with realistic truck numbers (e.g. 540 kWh pack, ~1.2 kWh/km).
   - Each edge case from step 4.
   - Unavailable input → "can't compute" result.
6. **Consume it** from the ViewModel by injection; the ViewModel maps the result into State, the use case knows nothing about UI.

## Project conventions
- `:core:domain` has zero Android dependencies; it must build as a plain Kotlin/JVM module.
- Keep thresholds (e.g. reserve margin for the low-battery warning) as named constants or constructor parameters, not magic numbers.
- Phase 2 (cargo-weight adjustment) is not built yet; do not add cargo parameters now.

## Final checklist
- [ ] Single operation, named `<Verb><Noun>UseCase`.
- [ ] Pure Kotlin, domain types only.
- [ ] Unavailable inputs produce an explicit result.
- [ ] Edge cases tested; `./gradlew :core:domain:test` passes.
