---
name: new-vehicle-property
description: Wire a VHAL vehicle property end to end (VehiclePropertyIds check, manifest permission, CarVehicleDataSource mapping with throttling, unavailable/denied handling, domain model, fake data source, simulate-trip script). Use when asked to read a new vehicle value or when a screen needs data that VehicleState does not expose yet.
---

# New vehicle property

## When to use
- Reading a new property from `CarPropertyManager` (speed, gear, battery, capacity, range, or a new one).
- Changing how an existing property is mapped, sampled or reported as unavailable.
- Phase 2 vendor properties (cargo, trailer) are out of scope until Phase 2 starts.

## Step by step
1. **Confirm the property.** Look up the ID, value type, unit, change mode (ON_CHANGE / CONTINUOUS / STATIC) and required permission in the current `VehiclePropertyIds` / VHAL docs. Write them down; do not trust the IDs in `CLAUDE.md` without checking.
2. **Manifest.** Add the `<uses-permission>` to `:app`'s manifest. Dangerous car permissions (e.g. `CAR_SPEED`, `CAR_ENERGY`) need a runtime request flow; handle the denied case.
3. **Domain** (`:core:domain`, no Android types):
   - Add the field to `VehicleState` using domain units (km/h, %, kWh, km) and a representation that can say "not available" (nullable or `Reading<T>`).
   - If a new enum is needed (like `Gear`), define it in the domain; map raw ints in `:core:data`.
4. **Data** (`:core:data`, `CarVehicleDataSource`):
   - Check availability first (`getCarPropertyConfig`, `isPropertyAvailable`) and catch `SecurityException` for missing permission → emit "not available", never crash.
   - Register with `registerCallback` / `subscribePropertyEvents` inside `callbackFlow`, unregister in `awaitClose`.
   - CONTINUOUS properties: request a modest sample rate and also `sample()`/`conflate()` the flow (e.g. speed ≤ 4 updates/s to the UI).
   - Convert units in a mapper (`<Property>Mapper.kt`), e.g. m/s → km/h, Wh → kWh, m → km. Keep mappers pure and unit-tested.
   - Run on `Dispatchers.Default`/IO via `flowOn`; Car API types never leave this module.
5. **Fake.** Extend `FakeVehicleDataSource` so tests and previews can set the new value and its unavailable state.
6. **Simulation.** Add the `adb shell cmd car_service inject-vhal-event <hex id> <value>` step to `scripts/simulate-trip.sh` with a comment giving the unit, and document it in the "Testing on the emulator" section if it is new.
7. **Tests:**
   - Mapper unit tests (unit conversion, unknown raw values).
   - `CarVehicleDataSource` mapping logic tested via the mapper; data source behaviour for unavailable/denied.
   - A ViewModel or snapshot test showing the "not available" UI.
8. **Verify on the emulator** (Automotive image): inject values with adb (`adb root` first) and check the UI updates and degrades correctly when the property is missing.

## Project conventions
- Screens and use cases only see `VehicleDataSource`; nothing outside `:core:data` imports `android.car`.
- Driving mode is not a property: it comes from `CarUxRestrictionsManager` as `Flow<DrivingMode>`.
- Update the properties table in `.claude/CLAUDE.md` when adding a property.

## Final checklist
- [ ] ID, unit and permission confirmed against current docs.
- [ ] Permission declared; denied → "not available".
- [ ] Unavailable property → "not available", no crash.
- [ ] Continuous properties throttled before reaching the UI.
- [ ] Callback unregistered in `awaitClose`.
- [ ] Fake, mapper tests and simulate script updated.
- [ ] `./gradlew test lint` passes.
