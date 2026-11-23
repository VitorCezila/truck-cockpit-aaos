#!/usr/bin/env bash
# Plays a trip on an Android Automotive emulator by injecting VHAL events:
# parked -> drive -> cruise (light energy use) -> climb (heavy energy use) -> brake -> park.
#
# For the Trip screen, pick "Campinas Distribution Center" (96 km) while parked, before the truck
# moves. The cruise phase covers the first km, so the average use shows up and the destination is
# reachable; the climb raises the average until the battery no longer reaches it and the
# "Battery won't reach destination" warning appears.
#
# Usage: scripts/simulate-trip.sh [step-seconds] [phase-seconds]
#   step-seconds:  length of each short step (default 2)
#   phase-seconds: length of the cruise and climb phases (default 60; 60 s at 80 km/h is ~1.3 km)
# Needs a rooted AAOS emulator (the script runs `adb root`).
set -euo pipefail

STEP="${1:-2}"
PHASE="${2:-60}"

# VehiclePropertyIds (checked against the android.car library, SDK 37).
PERF_VEHICLE_SPEED=0x11600207   # float, m/s, CONTINUOUS
GEAR_SELECTION=0x11400400       # int32, ON_CHANGE, VehicleGear: 4 = PARK, 8 = DRIVE
EV_BATTERY_LEVEL=0x11600309     # float, Wh, CONTINUOUS. Keep below INFO_EV_BATTERY_CAPACITY (STATIC,
                                # 150000 Wh on the API 35 emulator) or the percentage clamps at 100%.
RANGE_REMAINING=0x11600308      # float, meters, CONTINUOUS

GEAR_PARK=4
GEAR_DRIVE=8

# ON_CHANGE properties keep an injected value.
inject() {
  adb shell cmd car_service inject-vhal-event "$1" "$2" >/dev/null
}

# The emulator keeps publishing its own values for CONTINUOUS properties, which overwrite a
# one-off inject-vhal-event at once. Stream the value for the whole state instead (returns at once).
inject_continuous() {
  adb shell cmd car_service inject-continuous-events "$1" "$2" -s "$3" -d "$4" >/dev/null
}

# state <speed m/s> <gear> <battery Wh> <range m> [seconds]
state() {
  local speed_ms=$1 gear=$2 battery_wh=$3 range_m=$4 seconds=${5:-$STEP}
  echo "speed=${speed_ms} m/s gear=${gear} battery=${battery_wh} Wh range=${range_m} m for ${seconds}s"
  inject "$GEAR_SELECTION" "$gear"
  inject_continuous "$PERF_VEHICLE_SPEED" "$speed_ms" 10 "$seconds"
  inject_continuous "$EV_BATTERY_LEVEL" "$battery_wh" 1 "$seconds"
  inject_continuous "$RANGE_REMAINING" "$range_m" 1 "$seconds"
  sleep "$seconds"
}

adb root >/dev/null
adb wait-for-device

# 50 kWh at ~0.4 kWh/km: Campinas (96 km) needs ~38 kWh plus the 7.5 kWh reserve.
echo "Parked: pick a destination on the Trip tab now"
state 0    "$GEAR_PARK"  50000 125000 "$((STEP * 5))"

echo "Drive, accelerating"
state 0    "$GEAR_DRIVE" 50000 125000
state 8.3  "$GEAR_DRIVE" 49980 124900   # ~30 km/h
state 16.7 "$GEAR_DRIVE" 49950 124800   # ~60 km/h

echo "Cruising at ~80 km/h, light energy use (~0.4 kWh/km): destination reachable"
state 22.2 "$GEAR_DRIVE" 49900 124700 "$((PHASE / 2))"
state 22.2 "$GEAR_DRIVE" 49600 124000 "$((PHASE / 2))"

echo "Climbing with a full load, heavy energy use: the average rises, destination unreachable"
state 22.2 "$GEAR_DRIVE" 48800 104000 "$((PHASE / 3))"
state 22.2 "$GEAR_DRIVE" 47900 82000  "$((PHASE / 3))"
state 22.2 "$GEAR_DRIVE" 47000 60000  "$((PHASE / 3))"

echo "Braking"
state 11.1 "$GEAR_DRIVE" 46990 59900    # ~40 km/h
state 0    "$GEAR_DRIVE" 46990 59900

echo "Parked"
state 0    "$GEAR_PARK"  46990 59900
