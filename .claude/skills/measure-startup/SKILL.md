---
name: measure-startup
description: Measure cold start with Macrobenchmark, generate and check in the Baseline Profile, and record before/after numbers in the README. Use when asked about Baseline Profiles, startup time, or before a release/portfolio update.
---

# Measure startup

## When to use
- First time setting up the Baseline Profile.
- After a change that affects startup (new module, DI graph, first screen).
- Before tagging a release or updating the README numbers.

## Step by step
1. **Device.** Start an AAOS emulator (Automotive system image, same one every time) and note its name, API level and host machine. Benchmarks are only comparable on the same setup.
2. **Module check.** `:baselineprofile` targets `:app` and has:
   - a `BaselineProfileGenerator` that cold-starts the app and walks the critical journey: Dashboard → Trip → back.
   - a `StartupBenchmark` with `StartupMode.COLD` and two compilation modes: `CompilationMode.None()` (before) and `CompilationMode.Partial(BaselineProfileMode.Require)` (after). Use at least 10 iterations.
3. **Before.** Run `./gradlew :baselineprofile:connectedCheck` and read `timeToInitialDisplayMs` (median and P90) for the `None` run from the benchmark JSON in `baselineprofile/build/outputs/`.
4. **Generate.** `./gradlew :app:generateBaselineProfile`. Confirm the profile file was updated under `app/src/release/generated/baselineProfiles/` (or the path configured by the plugin) and check it in.
5. **After.** Run `./gradlew :baselineprofile:connectedCheck` again and read the numbers for the profile run.
6. **README.** Update the "Startup" table: date, device/emulator, iterations, median and P90 for before and after, and the % change. Never round in the project's favour; copy numbers from the JSON.
7. **Commit** with `perf: update baseline profile` or `docs: update startup numbers`.

## Project conventions
- Both commands need an AAOS emulator; they will not run in plain JVM CI.
- Release builds only: benchmarks use the `benchmark`/`nonMinifiedRelease` variant the plugin creates, not debug (LeakCanary skews numbers).
- Use the latest stable `androidx.benchmark` and `baselineprofile` plugin; versions live in `gradle/libs.versions.toml`.

## Final checklist
- [ ] Same emulator for before and after, noted in the README.
- [ ] Baseline Profile file regenerated and committed.
- [ ] README table updated with median and P90, copied from the JSON output.
