# Stitch prompts: Truck Cockpit (AAOS)

Paste prompt 1 in Stitch first (desktop/web layout, landscape). Then add the other screens one by one in the same project, so they share the visual style.

---

## Prompt 1: Dashboard (parked)

Design a landscape in-vehicle infotainment screen (1920x1080, wide automotive head unit) for "Truck Cockpit", a driver dashboard for electric trucks running on Android Automotive OS.

Style: dark theme, very high contrast, glanceable. Near-black background (#0E1113), surfaces in dark graphite (#1A1F23), primary accent electric green (#3DDC84) for healthy values, amber (#FFB020) for warnings, red (#FF4D4F) for critical. Font: Inter. Big numbers, few words. Large touch targets (at least 76dp). Material 3, no decorative illustrations, no gradients.

Layout:
- Left half: a large speed readout ("82 km/h") with the current gear in a big pill next to it ("D").
- Right half, a 2x2 grid of cards: Battery (78% with a horizontal bar and "412 kWh of 540 kWh"), Range ("296 km"), Energy use ("1.21 kWh/km"), Next charging stop ("Graça Station, 140 km").
- Top status bar: time, outside temperature, a small "Parked" chip.
- Bottom navigation with 2 large tabs: "Dashboard" (selected) and "Trip".

---

## Prompt 2: Dashboard (driving mode)

Same app and style. The truck is moving, so the screen switches to a reduced driving layout.

- Only three elements: very large speed ("82 km/h"), gear pill ("D"), and range ("296 km") with a battery bar.
- Bigger text than the parked screen, more empty space, no secondary cards.
- Top status chip changes to "Driving".
- Bottom navigation still visible, with larger tabs. No other buttons.

---

## Prompt 3: Trip (parked)

Same app and style. Trip screen.

- Destination card at the top: "Campinas Distribution Center", "186 km left", "ETA 15:40".
- Energy card: "Battery at arrival: 34%" with a bar, and "Average use: 1.18 kWh/km".
- A simple line chart of battery level over the trip (start 92%, now 78%, projected arrival 34%).
- A "Change destination" button at the bottom.
- Bottom navigation: "Trip" selected.

---

## Prompt 4: Trip, low-battery warning (driving mode)

Same app and style. Driving mode, battery will not reach the destination.

- Full-width amber warning banner: "Battery won't reach destination", with "Arrival estimate: -8%" below it.
- One large action button: "Route to nearest charger (24 km)".
- Speed and range still visible in a compact top row.
- No chart, no secondary information.
