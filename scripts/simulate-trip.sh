#!/usr/bin/env bash
# Plays a short trip on an Android Automotive emulator by injecting VHAL events:
# parked -> drive -> accelerate (battery and range drain) -> brake -> park.
#
# Usage: scripts/simulate-trip.sh [step-seconds]
# Needs a rooted AAOS emulator (the script runs `adb root`).
set -euo pipefail

STEP="${1:-2}"

# VehiclePropertyIds (checked against the android.car library, SDK 37).
PERF_VEHICLE_SPEED=0x11600207   # float, m/s
GEAR_SELECTION=0x11400400       # int32, VehicleGear: 4 = PARK, 8 = DRIVE
EV_BATTERY_LEVEL=0x11600309     # float, Wh. Keep below INFO_EV_BATTERY_CAPACITY (STATIC, ~150000 Wh on
                                # the stock emulator VHAL; check yours) or the percentage clamps at 100%.
RANGE_REMAINING=0x11600308      # float, meters

GEAR_PARK=4
GEAR_DRIVE=8

inject() {
  adb shell cmd car_service inject-vhal-event "$1" "$2"
}

state() {
  local speed_ms=$1 gear=$2 battery_wh=$3 range_m=$4
  echo "speed=${speed_ms} m/s gear=${gear} battery=${battery_wh} Wh range=${range_m} m"
  inject "$PERF_VEHICLE_SPEED" "$speed_ms"
  inject "$GEAR_SELECTION" "$gear"
  inject "$EV_BATTERY_LEVEL" "$battery_wh"
  inject "$RANGE_REMAINING" "$range_m"
  sleep "$STEP"
}

adb root >/dev/null
adb wait-for-device

echo "Parked"
state 0    "$GEAR_PARK"  120000 296000

echo "Drive, accelerating"
state 0    "$GEAR_DRIVE" 120000 296000
state 8.3  "$GEAR_DRIVE" 119500 295000   # ~30 km/h
state 16.7 "$GEAR_DRIVE" 118800 293000   # ~60 km/h
state 22.2 "$GEAR_DRIVE" 117600 290000   # ~80 km/h

echo "Cruising, battery draining"
state 22.2 "$GEAR_DRIVE" 110000 272000
state 22.2 "$GEAR_DRIVE" 98000 243000
state 22.2 "$GEAR_DRIVE" 86000 214000

echo "Braking"
state 11.1 "$GEAR_DRIVE" 85600 213000   # ~40 km/h
state 0    "$GEAR_DRIVE" 85600 213000

echo "Parked"
state 0    "$GEAR_PARK"  85600 213000
