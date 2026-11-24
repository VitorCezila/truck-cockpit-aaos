---
name: measure-startup
description: Measure cold start, generate and check in the Baseline Profile, and record before/after numbers in the README. Use when asked about Baseline Profiles, startup time, or before a release/portfolio update.
---

# Measure startup

## When to use
- After a change that affects startup (new module, DI graph, first screen, new journey).
- Before tagging a release or updating the README numbers.

## Why a script, not the Gradle tasks
AAOS runs apps as the driver (user 10); user 0 is the headless system user. Jetpack Benchmark assumes user 0, so on AAOS:
- `:app:generateBaselineProfile` runs the generator, but Gradle pulls the output from user 0's storage and reports "No baseline profile rules were generated";
- Macrobenchmark fails with `SHELL-ACCESS-DENIED`; with that suppressed it still can't write its Perfetto trace, and ProfileInstaller can't install the profile (`RESULT_NOT_WRITABLE`).

`scripts/baseline-profile.sh` runs the generator as the current user and measures cold start through the shell. Its header documents the method.

## Step by step
1. **Device.** Start the AAOS emulator (Automotive 1408p landscape, "Android Automotive with Google APIs" x86_64, API 35; same one every time). Note the AVD name, API level and host machine (CPU, RAM): emulator numbers are only comparable on the same setup. Close heavy host apps; the same mode's median moved between about 1 s and 2 s across sessions, depending on host load.
2. **Generate.** `scripts/baseline-profile.sh generate`. It installs `nonMinifiedRelease`, runs `BaselineProfileGenerator` (cold start, Dashboard → Trip → Dashboard) and writes `app/src/release/generated/baselineProfiles/{baseline,startup}-prof.txt`. Check that the files contain `com/cezila/truckcockpit` rules, then commit them.
3. **Measure.** `scripts/baseline-profile.sh benchmark 30`. It installs `benchmarkRelease` (release-like, no LeakCanary), then alternates rounds of 5 cold launches between:
   - `none`: `cmd package compile -m verify`, no AOT code (before);
   - `baseline-profile`: the APK's `baseline.prof` installed for the driver, then `-m speed-profile` (after).
   It writes `build/startup-benchmark/startup.csv` (every launch) and `summary.txt` (median and P90 TTID per mode).
4. **README.** Update the "Startup" table from `summary.txt`: date, emulator/API/host, launches, median and P90 for both modes, and the % change. Never round in the project's favour, and say so when the difference is within the noise.
5. **Commit** with `perf: update baseline profile` or `docs: update startup numbers`.

## Project conventions
- Both modes need a rooted AAOS emulator (Google APIs image, not Google Play); nothing here runs in CI. CI only compiles `:baselineprofile` (`compileNonMinifiedReleaseKotlin`).
- Benchmarks use release-like builds (`benchmarkRelease`/`nonMinifiedRelease`, created by the `androidx.baselineprofile` plugin), never debug: LeakCanary skews numbers.
- Use the latest stable `androidx.benchmark` and `baselineprofile` plugin; versions live in `gradle/libs.versions.toml`.

## Final checklist
- [ ] Same emulator and host for every number in the table, noted in the README.
- [ ] Baseline Profile files regenerated and committed.
- [ ] README table copied from `summary.txt`, with median and P90.
