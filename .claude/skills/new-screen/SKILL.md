---
name: new-screen
description: Create or extend a screen in a :feature:* module (MVI contract, ViewModel, stateless parked and driving composables, previews, Roborazzi tests). Use when asked for a new screen, a new feature module such as :feature:trip, or a new card/section on the Dashboard or Trip screens.
---

# New automotive screen

## When to use
- Adding a screen (e.g. Trip) or a new feature module.
- Adding a card, banner or section to an existing screen.
- Not for vehicle data plumbing: use `new-vehicle-property` first if the screen needs a property that is not in `VehicleState` yet.

## Step by step
1. **Module.** If the feature module does not exist, create `feature/<name>/` with a `build.gradle.kts` that applies the feature convention plugin from `build-logic/` (never configure Android/Compose/Hilt by hand). Register it in `settings.gradle.kts` and add it to `:app`. Allowed dependencies: `:core:domain`, `:core:ui` only.
2. **Contract** in `com.cezila.truckcockpit.feature.<name>`, file `<Name>Contract.kt`:
   - `data class <Name>State(...)` — immutable, includes `drivingMode: DrivingMode` and explicit "not available" values (nullable or a sealed `Reading<T>`), never fake zeros.
   - `sealed interface <Name>Intent`
   - `sealed interface <Name>Effect` (one-shot: navigation, toasts).
3. **ViewModel** `<Name>ViewModel` (`@HiltViewModel`):
   - Inject use cases and/or `VehicleDataSource` interfaces from `:core:domain`. No `android.car` imports.
   - Combine `vehicleState` and `drivingMode` flows into `StateFlow<<Name>State>` with `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)`.
   - Update state only with `.copy()`. Effects through a `Channel` exposed as `Flow`.
   - `fun onIntent(intent: <Name>Intent)` is the single entry point.
4. **Route + screen composables:**
   - `<Name>Route(viewModel = hiltViewModel())` collects with `collectAsStateWithLifecycle()` and effects with a lifecycle-aware collector. This is the only composable that sees the ViewModel.
   - `<Name>Screen(state, onIntent)` is stateless and branches on `state.drivingMode`:
     - `<Name>ParkedContent` — full layout.
     - `<Name>DrivingContent` — reduced layout: only the 2–3 most important values, larger type, larger targets.
   - Reuse `:core:ui` components (gauges, big buttons, alert banner). If a component is useful in another screen, move it to `:core:ui`.
5. **Navigation.** Add the destination to the NavHost in `:app`. The tab bar stays visible in driving mode with large tabs.
6. **Previews.** `@Preview` for parked and driving, at the target display size (landscape 1920x1080), dark theme, state built directly or from `FakeVehicleDataSource` values. Include one "not available" preview.
7. **Tests:**
   - ViewModel test (JUnit + Turbine) with `FakeVehicleDataSource`: initial state, a vehicle update, the switch to driving mode and back, unavailable data.
   - Roborazzi tests in `src/test`: at least `parked`, `driving` and `not_available` snapshots at the target size. Record with `./gradlew recordRoborazziDebug`, then check with `./gradlew verifyRoborazziDebug`.

## Project conventions
- Dark theme by default, high contrast; colors come from the `:core:ui` theme, never hard-coded.
- Touch targets ≥ 76dp. Main value of each card readable in < 2 s (big number, short label).
- Driving variant: no text input, no video, no scrolling lists, no decorative animations.
- The hosting activity must keep `distractionOptimized` = true in the manifest.

## Final checklist
- [ ] Contract has State/Intent/Effect; State is immutable and models "not available".
- [ ] Composables take state + callbacks; only the Route touches the ViewModel.
- [ ] Parked and driving variants both exist and both have Roborazzi snapshots.
- [ ] No `android.car` import in the feature module.
- [ ] Targets ≥ 76dp, no scrolling/inputs in driving mode.
- [ ] `./gradlew test verifyRoborazziDebug lint` passes.
