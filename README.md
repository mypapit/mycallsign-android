## Introduction

MYCallsign is a native Android application for looking up Malaysian amateur radio callsigns offline, viewing allocation statistics, and optionally monitoring live Malaysian APRS stations.

## Overview & Features

MYCallsign 5.0 is built with Kotlin, Jetpack Compose, and Material 3 for Android 10 (API 29) through Android 17 (API 37), with no ads, no activity tracking, and strict on-device privacy.

- **Callsign Directory & Sync**: Downloads the latest callsign directory from remote application server on first launch (with a bundled offline snapshot fallback if unreachable), encourages weekly updates, and enforces a 12-hour update cooldown with HTTP conditional caching (`ETag` / `Last-Modified`).
- **Flexible Search & Filters**: Search by **Callsign**, wildcard **Name** (supports `*`, minimum 3 characters), or **Apparatus Assignment No.**, and filter results by license status (`Active`, `Expired`) or Malaysian callsign prefix (`9M2`, `9M4`, `9M6`, `9M8`, `9W2`, `9W6`, `9W8`).
- **Callsign Details & Persistent Favorites**: View assignment holder details, Apparatus Assignment number, expiry date, and status badges (`ACTIVE`, `EXPIRED`, `EXPIRED >5Y`), copy or share details, and save Favorites that persist across database updates.
- **Offline Statistics**: Inspect total, active, expired, and expiring-within-90-days counts, explore an interactive class distribution chart (`9M`, `9W2`, `9W6`, `9W8`, `Others`), and browse callsigns expired more than 5 years ago.
- **Optional Live Malaysian APRS Map**: Opt-in MapLibre GeoJSON viewer fed by `https://api.mypapit.net/repeater/stations.json` (refreshed every 30 minutes) over OpenFreeMap dark tiles, with station detail sheets and directory cross-lookup. Location detection is off by default, never requested at startup, stays strictly on-device, and can be disabled anytime.

## Screenshots

| Callsign Directory & Search | Optional Live APRS Map | Offline Directory Statistics |
| :---: | :---: | :---: |
| ![Callsign Directory & Search](screenshots/Screenshot%202026-11-25%20171612.png) | ![Optional Live APRS Map](screenshots/Screenshot%202026-11-25%20171643.png) | ![Offline Directory Statistics](screenshots/Screenshot%202026-11-25%20171659.png) |

## Credits

- **Author**: Mohammad Hafiz bin Ismail (`mypapit@gmail.com`)
- **Source Code**: [https://github.com/mypapit/mycallsign-android](https://github.com/mypapit/mycallsign-android)
- **Facebook**: [https://fb.me/mycallsign](https://fb.me/mycallsign)
- **Special Thanks**: `9W2SHB Mysaleh.net` for providing the callsign database.
- **Open-Source Libraries & Map Data**:
  - AndroidX, Jetpack Compose, Room, and DataStore (Apache License 2.0)
  - OkHttp and Kotlinx Serialization (Apache License 2.0)
  - MapLibre Native Android SDK (BSD License)
  - OpenFreeMap and OpenStreetMap contributors (ODbL)
- **Disclaimer**: This application is not created nor endorsed by the Malaysian Communications & Multimedia Commission (MCMC).

## License

Copyright © 2015–2026 Mohammad Hafiz bin Ismail

This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the full GNU General Public License at [https://gnu.org](https://gnu.org) ([GNU GPL v3](https://www.gnu.org/licenses/gpl-3.0.html)).
