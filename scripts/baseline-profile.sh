#!/usr/bin/env bash
# Generates the Baseline Profile and measures cold start on an Android Automotive emulator.
#
# Why not the Gradle tasks (`:app:generateBaselineProfile`, Macrobenchmark `connectedCheck`)?
# AAOS runs apps as the driver (user 10); user 0 is the headless system user. Jetpack Benchmark
# assumes user 0 in several places, so on AAOS:
#   - the generator passes, but Gradle pulls its output from user 0's /sdcard and finds nothing
#     ("No baseline profile rules were generated");
#   - Macrobenchmark fails with SHELL-ACCESS-DENIED, and even with that suppressed it can't write
#     its Perfetto trace or install the profile (ProfileInstaller writes to user 0's profile,
#     RESULT_NOT_WRITABLE).
# This script runs the generator with `am instrument --user <current>` and pulls its output with
# root, and measures cold start the way Macrobenchmark does, through the shell:
#   - none:             `cmd package compile --reset`, then `-m verify` (no AOT code).
#   - baseline-profile: `--reset`, the APK's baseline.prof copied into the driver's current
#                       profile, then `-m speed-profile` (what Play does with the profile).
# Each launch: force-stop, drop the page cache, `am start -W`; TotalTime is the time to initial
# display (TTID). The two modes alternate in rounds of 5 launches.
#
# Usage: scripts/baseline-profile.sh generate               # app/src/release/generated/baselineProfiles/
#        scripts/baseline-profile.sh benchmark [iterations] # build/startup-benchmark/ (default 20 per mode)
# Needs a rooted AAOS emulator (the script runs `adb root`), adb on PATH and JAVA_HOME for Gradle.
set -euo pipefail

MODE="${1:?usage: $0 generate|benchmark [iterations]}"
ITERATIONS="${2:-20}"
ROUND_SIZE=5
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
APP_PACKAGE=com.cezila.truckcockpit
TEST_PACKAGE=com.cezila.truckcockpit.baselineprofile

# Git Bash on Windows rewrites absolute device paths such as /data/... unless told not to. Host
# paths given to adb are then kept relative (push and pull from inside the folder).
export MSYS_NO_PATHCONV=1

adb root >/dev/null
adb wait-for-device
USER_ID="$(adb shell am get-current-user | tr -d '\r')"

generate() {
  local device_output="/data/media/$USER_ID/Android/media/$TEST_PACKAGE"
  local out="$ROOT/app/src/release/generated/baselineProfiles"

  echo "Installing the app and the generator..."
  "$ROOT/gradlew" -p "$ROOT" :app:installNonMinifiedRelease :baselineprofile:installNonMinifiedRelease -q
  adb shell rm -rf "$device_output"

  echo "Running BaselineProfileGenerator as user $USER_ID..."
  local result
  result="$(adb shell am instrument -w --user "$USER_ID" \
    -e class "$TEST_PACKAGE.BaselineProfileGenerator" \
    -e androidx.benchmark.suppressErrors EMULATOR \
    "$TEST_PACKAGE/androidx.test.runner.AndroidJUnitRunner" | tr -d '\r')"
  if ! echo "$result" | grep -q '^OK ('; then
    echo "$result" | tail -n 30
    echo "Generator failed." >&2
    exit 1
  fi

  # With includeInStartupProfile = true the rule writes one file (the latest copy has no date
  # suffix). It is both the startup profile and the Baseline Profile.
  mkdir -p "$out"
  (cd "$out" && adb pull "$device_output/BaselineProfileGenerator_generate-startup-prof.txt" startup-prof.txt >/dev/null)
  cp "$out/startup-prof.txt" "$out/baseline-prof.txt"
  wc -l "$out"/*.txt
}

# Prints the TTID in ms of one cold launch.
cold_launch() {
  adb shell am force-stop --user "$USER_ID" "$APP_PACKAGE"
  adb shell "sync; echo 3 > /proc/sys/vm/drop_caches"
  sleep 1
  adb shell am start -W -S --user "$USER_ID" -n "$APP_PACKAGE/.MainActivity" \
    | tr -d '\r' | awk -F': ' '/^TotalTime/ { print $2 }'
}

compile_none() {
  adb shell cmd package compile --reset "$APP_PACKAGE" >/dev/null
  adb shell cmd package compile -f -m verify "$APP_PACKAGE" >/dev/null
}

compile_baseline_profile() {
  local profile_dir="/data/misc/profiles/cur/$USER_ID/$APP_PACKAGE"
  adb shell cmd package compile --reset "$APP_PACKAGE" >/dev/null
  (cd "$WORK" && adb push baseline.prof /data/local/tmp/truckcockpit-baseline.prof >/dev/null)
  adb shell "cp /data/local/tmp/truckcockpit-baseline.prof $profile_dir/primary.prof \
    && chown \$(stat -c %u:%g $profile_dir) $profile_dir/primary.prof \
    && rm /data/local/tmp/truckcockpit-baseline.prof"
  adb shell cmd package compile -f -m speed-profile "$APP_PACKAGE" >/dev/null
}

# median and p90 (linear interpolation between closest ranks) of the numbers on stdin.
summary() {
  sort -n | awk '
    { v[NR] = $1 }
    function pct(p,   r, lo) { r = (NR - 1) * p + 1; lo = int(r); return v[lo] + (r - lo) * (v[lo + 1] - v[lo]) }
    END { v[NR + 1] = v[NR]; printf "min=%d median=%.1f p90=%.1f max=%d n=%d\n", v[1], pct(0.5), pct(0.9), v[NR], NR }'
}

benchmark() {
  local out="$ROOT/build/startup-benchmark"
  WORK="$out"
  mkdir -p "$out"

  echo "Installing the benchmark build of the app..."
  "$ROOT/gradlew" -p "$ROOT" :app:installBenchmarkRelease -q
  local apk
  apk="$(ls "$ROOT"/app/build/outputs/apk/benchmarkRelease/*.apk | head -n 1)"
  (cd "$out" && unzip -o -q -j "$apk" assets/dexopt/baseline.prof)

  # Runtime permissions granted up front, so the permission dialog stays out of the launch.
  for permission in android.car.permission.CAR_SPEED android.car.permission.CAR_ENERGY; do
    adb shell pm grant --user "$USER_ID" "$APP_PACKAGE" "$permission"
  done

  # The modes alternate in rounds of ROUND_SIZE launches, so emulator drift (host load, thermal)
  # hits both modes alike instead of whichever runs second.
  local csv="$out/startup.csv"
  local rounds=$(((ITERATIONS + ROUND_SIZE - 1) / ROUND_SIZE))
  echo "mode,round,iteration,ttid_ms" >"$csv"
  for round in $(seq 1 "$rounds"); do
    for mode in none baseline-profile; do
      if [ "$mode" = none ]; then compile_none; else compile_baseline_profile; fi
      echo "Round $round/$rounds, $mode: $(adb shell dumpsys package dexopt | tr -d '\r' | grep -A2 "\[$APP_PACKAGE\]" | tail -n 1 | grep -o 'status=[a-z-]*')"
      cold_launch >/dev/null # warm-up launch after compiling, not recorded
      for i in $(seq 1 "$ROUND_SIZE"); do
        echo "$mode,$round,$i,$(cold_launch)" >>"$csv"
      done
    done
  done
  adb shell am force-stop --user "$USER_ID" "$APP_PACKAGE"

  {
    echo "Date:     $(date -u +%Y-%m-%dT%H:%MZ)"
    echo "Device:   $(adb emu avd name | head -n 1 | tr -d '\r'), API $(adb shell getprop ro.build.version.sdk | tr -d '\r')"
    echo "Launches: $((rounds * ROUND_SIZE)) cold launches per mode, in $rounds alternating rounds (TTID, ms)"
    for mode in none baseline-profile; do
      printf '%-17s %s\n' "$mode:" "$(awk -F, -v m="$mode" '$1 == m { print $4 }' "$csv" | summary)"
    done
  } | tee "$out/summary.txt"
}

case "$MODE" in
  generate) generate ;;
  benchmark) benchmark ;;
  *)
    echo "usage: $0 generate|benchmark [iterations]" >&2
    exit 1
    ;;
esac
