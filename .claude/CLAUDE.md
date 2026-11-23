# Truck Cockpit (AAOS)

Driver dashboard for electric trucks, running on Android Automotive OS (AAOS). It reads vehicle data (speed, gear, battery, range) through the Car API and simplifies its UI automatically while the truck is moving.

Portfolio project. Goals, in order: show real AAOS skills (`CarPropertyManager`, UX restrictions, distraction optimization), keep a high quality bar (snapshot tests, LeakCanary, measured startup), and stay small enough to finish.

No backend, no accounts, no network. Everything runs on the head unit.

## Scope

**Phase 1 (current)**
1. **Dashboard:** speed, gear, battery level, estimated range.
2. **Trip:** energy use per km and a warning when the battery won't reach the destination.
3. **Driving mode:** when the vehicle moves, the UI switches to a reduced layout (fewer elements, larger text and touch targets); it returns to the full layout when parked.

**Phase 2 (later, do not build yet)**
- Simulated OEM/vendor data (cargo weight, trailer attached), behind the same `VehicleDataSource` interface.
- Range estimate adjusted by cargo weight.

## Stack

- Kotlin, Jetpack Compose, Material 3
- MVI + Clean Architecture, multi-module Gradle with convention plugins in `build-logic/`
- Hilt, Coroutines + Flow
- Car API: `android.car` (`Car`, `CarPropertyManager`, `CarUxRestrictionsManager`), enabled with `useLibrary("android.car")`
- Tests: JUnit, Turbine, Roborazzi (snapshot tests on the JVM)
- Quality: LeakCanary (debug), Baseline Profiles + Macrobenchmark
- CI: GitHub Actions

Use the latest stable versions; confirm them before adding a dependency. Keep versions in `gradle/libs.versions.toml`.

## Architecture

```
:app → :feature:dashboard, :feature:trip, :core:ui, :core:data, :core:domain
:feature:* → :core:domain, :core:ui
:core:data → :core:domain
:core:ui   → :core:domain
:baselineprofile → :app   (Baseline Profile generator + Macrobenchmark)
```

| Module | Package | Contents |
|---|---|---|
| `:app` | `com.cezila.truckcockpit` | Activity, NavHost, Hilt wiring, manifest (automotive feature, permissions, `distractionOptimized`) |
| `:core:domain` | `com.cezila.truckcockpit.core.domain` | Models (`VehicleState`, `Gear`, `DrivingMode`), `VehicleDataSource` interface, use cases (e.g. `EstimateRangeUseCase`, `CheckDestinationReachableUseCase`) |
| `:core:data` | `com.cezila.truckcockpit.core.data` | `CarVehicleDataSource` (Car API), `FakeVehicleDataSource` (tests and previews), mappers |
| `:core:ui` | `com.cezila.truckcockpit.core.ui` | Theme, typography, shared composables (gauges, big buttons, alert banner) |
| `:feature:dashboard` | `com.cezila.truckcockpit.feature.dashboard` | Dashboard screen, ViewModel, contract (State/Intent/Effect) |
| `:feature:trip` | `com.cezila.truckcockpit.feature.trip` | Trip screen, ViewModel, contract |
| `:baselineprofile` | `com.cezila.truckcockpit.baselineprofile` | Baseline Profile generator, startup benchmarks |

## Vehicle data

All vehicle data goes through `VehicleDataSource` in `:core:domain`. Screens and use cases never touch the Car API directly.

- `CarVehicleDataSource` wraps `CarPropertyManager` and exposes `Flow<VehicleState>`.
- `FakeVehicleDataSource` emits any state you set. Use it in unit tests, snapshot tests and Compose previews.
- Driving mode comes from `CarUxRestrictionsManager`, exposed as `Flow<DrivingMode>`.

Phase 1 properties (`VehiclePropertyIds`) and permissions:

| Data | Property | Permission |
|---|---|---|
| Speed | `PERF_VEHICLE_SPEED` | `android.car.permission.CAR_SPEED` |
| Gear | `GEAR_SELECTION` | `android.car.permission.CAR_POWERTRAIN` |
| Battery level | `EV_BATTERY_LEVEL` | `android.car.permission.CAR_ENERGY` |
| Battery capacity | `INFO_EV_BATTERY_CAPACITY` | `android.car.permission.CAR_INFO` |
| Range | `RANGE_REMAINING` | `android.car.permission.CAR_ENERGY` |

Always handle a property being unavailable or the permission being denied: show a clear "not available" state, never crash.

## Commands

```bash
./gradlew assembleDebug                      # Build debug APK
./gradlew test                               # Unit tests
./gradlew verifyRoborazziDebug               # Compare snapshots with the recorded ones
./gradlew recordRoborazziDebug               # Re-record snapshots (only after an intended UI change)
./gradlew lint                               # Lint
./gradlew :app:generateBaselineProfile       # Generate the Baseline Profile (AAOS emulator required)
./gradlew :baselineprofile:connectedCheck    # Startup benchmarks (AAOS emulator required)
```

## Testing on the emulator

Use an **Automotive** system image from the Android Studio Device Manager.

Change vehicle data through the emulator's extended controls (car data section) or with adb. The `car_service` shell command may need `adb root` first:

```bash
adb root
adb shell cmd car_service inject-vhal-event 0x11600207 22.2   # PERF_VEHICLE_SPEED, m/s (~80 km/h)
adb shell cmd car_service inject-vhal-event 0x11400400 8      # GEAR_SELECTION, 8 = GEAR_DRIVE
adb shell cmd car_service inject-vhal-event 0x11600309 45000  # EV_BATTERY_LEVEL, Wh
adb shell cmd car_service inject-vhal-event 0x11600308 120000 # RANGE_REMAINING, meters
```

Check property IDs and units against `VehiclePropertyIds` before relying on them. `scripts/simulate-trip.sh` plays a full trip (accelerate, change gear, drain battery, stop) with these commands.

## Conventions

- MVI: one immutable `State` per screen, emitted via `.copy()`. Intents in, effects out.
- One use case = one operation.
- Interfaces in `:core:domain`, implementations in `:core:data`. Car API types never leak into the domain.
- `:core:domain` has zero Android dependencies.
- Composables are stateless: they receive state and callbacks, never a ViewModel.
- Every screen has a driving-mode variant and a parked variant. Both are covered by snapshot tests.
- Car API calls run off the main thread; collect flows with lifecycle-aware APIs.
- Commits follow Conventional Commits (`feat:`, `fix:`, `test:`, `chore:`, `docs:`).

## Workflow (mandatory)

Every change (feature, fix, maintenance, docs, tests, CI) goes through **issue → branch → PR → main**. Never commit or push directly to `main`.

1. **Issue** on GitHub (`gh issue create`) describing the change. One issue per change.
2. **Branch** from an up-to-date `main`, named `<type>/<issue-number>-<short-slug>`, e.g. `feat/12-dashboard-speed`, `fix/18-range-unavailable`, `docs/21-startup-numbers`. Types match Conventional Commits.
3. **Commits** on the branch in Conventional Commits format, scoped by module or screen.
4. **PR** to `main` (`gh pr create`) with `Closes #<issue>` in the body, after running the `merge-checklist` skill. CI must be green before merging.

Use the `start-task` skill to open the issue and branch, and `merge-checklist` to open the PR.

## Automotive UI rules

- Every activity shown while driving declares `<meta-data android:name="distractionOptimized" android:value="true"/>`; otherwise the system blocks it when the vehicle moves.
- Glanceable: the main value of each card readable in under 2 seconds.
- Large touch targets (at least 76dp) and high contrast. Dark theme by default.
- No text input, video, or scrolling lists while driving.
- No decorative animations.

## Quality gates

- **Snapshot tests:** Roborazzi for every screen, in parked and driving mode, at the target display size. CI runs `verifyRoborazziDebug`.
- **LeakCanary:** `debugImplementation` only. Fix any leak it reports before merging.
- **Startup:** Baseline Profile checked in. Record cold-start numbers (before and after) in the README.

## Watch out for

- The Car API exists only on AAOS. The app declares `android.hardware.type.automotive` as required; it does not run on phones.
- `CarPropertyManager` callbacks can fire very often (speed). Throttle or sample before updating the UI.
- Standard AAOS VHAL is car-oriented. Truck-specific data (cargo, trailer) does not exist there; on real trucks it comes from OEM vendor properties. That is Phase 2.
- Google Play only accepts some car app categories; this app is a portfolio project published on GitHub.

## Skills and agents

Project skills (`.claude/skills/`):

| Skill | Use it to |
|---|---|
| `start-task` | Open the GitHub issue and the `<type>/<issue>-<slug>` branch before any change |
| `new-screen` | Create or extend a screen: MVI contract, ViewModel, parked + driving composables, previews, Roborazzi tests |
| `new-vehicle-property` | Wire a VHAL property end to end: permission, `CarVehicleDataSource`, throttling, "not available", fake, simulate script |
| `new-use-case` | Add a pure-Kotlin use case in `:core:domain` with tests |
| `measure-startup` | Measure cold start, generate the Baseline Profile, update README numbers |
| `merge-checklist` | Run all quality gates, commit in Conventional Commits format and open the PR to `main` |

Project agents (`.claude/agents/`):

| Agent | Delegate when |
|---|---|
| `automotive-reviewer` | Screens, manifest or `:core:data` changed (read-only review of Car API and driving-mode rules) |
| `architecture-reviewer` | Before a PR or when modules/dependencies change (read-only review of module graph and MVI) |
| `test-writer` | A feature, screen or use case needs tests (writes only under `src/test`) |
