# KIDPOWER BAND PLATFORM

<img src="https://img.shields.io/badge/license-MIT-green" alt="license">
<img src="https://img.shields.io/badge/monorepo-multi--platform-blue" alt="monorepo">
<img src="https://img.shields.io/badge/status-maintained-yellow" alt="status">

<img width="113" height="156" alt="UNICEFUSA_KP" src="https://github.com/user-attachments/assets/1c6a33cd-4f9e-4835-8c33-f4b97ee9d81d">
<img width="250" height="234" alt="KID BAND" src="https://github.com/user-attachments/assets/fb2b02e9-405f-4cfd-80f0-f7a095f6bcdb">

About
-----
A monorepo of mobile apps and components for the UNICEF Kid Power / Calorie Cloud family:
- React Native app: Active For Good (afg-app-rn)
- Native Android: Calorie Cloud, Kid Power Home, Kid Power Community (cc-app-android, kph-app-android, kpc-app-android)
- Native iOS: Calorie Cloud (cc-app-ios)
- Integrations: Bluetooth PowerBand engine (BLE), HealthKit (iOS), Google Fit (Android), Lottie animations, CodePush

This README is a developer-first guide: quick start instructions, where core systems live, and a deep highlight of the Kid Power Band (BLE) engine so you can extend, port, or debug it.

Quick navigation
- Kid Power Band (BLE) engine — see "Kid Power Band (BLE) engine" below
- React Native entry — afg-app-rn/index.js
- RN API usage — afg-app-rn/app/api/APIManager.js (uses X-Access-Token)
- Android PowerBand engine — kph-app-android/.../powerband
- iOS HealthKit — cc-app-ios/Calorie Cloud/HealthKitManager.swift

Screenshot
----------
<img src="afg-app-rn/assets/images/Step3.png" alt="Onboarding / screenshot" width="480">

Quick Start (developer)
-----------------------

Prerequisites
- Node.js (LTS) + npm or yarn
- Android SDK & Android Studio
- Xcode & CocoaPods
- Java 8 (or JDK compatible with Gradle plugin used in apps)
- For RN debugging: react-native CLI (optional) and Metro running

React Native (AFG)
- Install deps:
  - cd afg-app-rn
  - npm install   # or yarn
- Start Metro:
  - npm start
- Run (example):
  - Android (from afg-app-rn or repo root): react-native run-android
  - iOS: cd cc-app-ios && pod install && open Calorie\ Cloud.xcworkspace && run from Xcode or react-native run-ios

Notes:
- RN entry: afg-app-rn/index.js registers Onboarding, Dashboard, Tracker.
- RN API: afg-app-rn/app/api/APIManager.js (expects BASE_URL + uses "X-Access-Token" header).

Android (native)
- Build:
  - ./cc-app-android/gradlew assembleDebug
- Install:
  - adb install -r cc-app-android/app/build/outputs/apk/debug/app-debug.apk
- Key files:
  - Manifest & permissions: cc-app-android/app/src/main/AndroidManifest.xml
  - Network API: cc-app-android/app/src/main/java/org/caloriecloud/android/util/APIManager.java

iOS (native)
- Install CocoaPods:
  - cd cc-app-ios
  - pod install
- Open workspace:
  - open "Calorie Cloud.xcworkspace"
- Build & Run from Xcode

Authentication / Example API call
--------------------------------
Apps use token-based auth. The header name encountered across RN/Android/iOS is X-Access-Token (case varies in code: X-Access-Token or x-access-token).

React Native usage (example)
- In afg-app-rn/app/api/APIManager.js, requests include:
  headers: { "X-Access-Token": accessToken }

curl example
- Replace BASE_URL and ACCESS_TOKEN:
  curl -H "X-Access-Token: ACCESS_TOKEN" "https://api.example.org/api/v2/challenges/user/current"

Kid Power Band (BLE) engine — highlight
--------------------------------------
The Kid Power Band is a BLE device. The canonical engine/implementation lives in the Android Kid Power app and is the reference implementation you should study or mirror when porting to another platform.

Where it lives (Android)
- kph-app-android/app/src/main/java/org/unicefkidpower/kid_power/Model/MicroService/Bluetooth/Powerband/
  - PowerBandDevice.java — device lifecycle, high-level orchestration
  - powerband/command/* — BandCommand, BandCommandResponse, Cmd* classes (CmdGetDailyData, CmdGetFirmwareVersion, CmdSetDeviceTime, etc.)
  - powerband/service/* — BatteryService.java, BleService.java, DataService.java, DeviceInformationService.java
  - command parsers (e.g., CmdGetFirmwareVersionParser) — binary parsing logic

How it works (high-level)
1. Scanning: BleScanner finds BLE peripherals, returns BlePeripheral/PowerBandDevice wrappers.
2. Command construction: each Cmd* builds a fixed-length byte[] packet with framing and checksum (see CmdGetDailyData.getBytes()).
3. Send & receive: Commands are written to the device's GATT write characteristic; responses are received, framed and parsed by BandCommandResponseParser into BandCommandResponse objects.
4. Higher-level services: DataService aggregates daily summaries and uploads them via the app REST APIs (APIManager pattern).
5. Sync & retry: Engine contains helpers for retries, chunking (block-based reads), and upload coordination.

Porting guidance / gotchas
- The protocol is binary: precise byte order, framing and checksums are required. Reuse the command formats exactly.
- Implement robust reconnect and retry strategies: BLE writes may fail; the Android engine contains retries and state machines (see PowerBandDevice).
- Responses may be chunked / multi-block — follow parsers’ assumptions about block numbering.
- If adding iOS support, implement a compatible BLE layer (CBCentralManager/CBPeripheral) and map Cmd* packet building/parsing directly.

Representative code (Java example)
- Command to get daily data (Android): kph-app-android/.../CmdGetDailyData.java has getBytes() building 16-byte packet with checksum.

Project structure (deep table)
------------------------------
Below is a practical, focused view of the repository structure (deep levels for the most important areas). Use this as an index — many more files exist in each module.

| Path | Description |
|---|---|
| / | Root of monorepo (multiple mobile apps & assets) |
| LICENSE | MIT license |
| README.md | This file |
| afg-app-rn/ | React Native app (Active For Good) |
| afg-app-rn/index.js | RN entry points (Onboarding, Dashboard, Tracker) |
| afg-app-rn/package.json | RN dependencies & scripts |
| afg-app-rn/app/api/APIManager.js | RN API helper — endpoints and X-Access-Token usage |
| afg-app-rn/app/screens/Onboarding.js | RN onboarding screen |
| afg-app-rn/assets/images/* | RN images & Lottie animations |
| cc-app-android/ | Calorie Cloud Android app (native) |
| cc-app-android/app/src/main/AndroidManifest.xml | App permissions & activities |
| cc-app-android/app/src/main/java/org/caloriecloud/android/util/APIManager.java | Android API calls (OkHttp-based), token handling |
| cc-app-android/app/src/main/res/* | Android layouts, drawables, assets |
| cc-app-ios/ | iOS (Calorie Cloud) |
| cc-app-ios/Calorie Cloud/AppDelegate.swift | App lifecycle, React bridge, CodePush, HealthKit initialization |
| cc-app-ios/Calorie Cloud/HealthKitManager.swift | HealthKit queries, daily aggregation & background delivery |
| cc-app-ios/Podfile | CocoaPods list (React, lottie, CodePush, Alamofire) |
| kph-app-android/ | Kid Power Home Android — contains canonical PowerBand engine |
| kph-app-android/app/src/main/java/org/unicefkidpower/kid_power/Model/MicroService/Bluetooth/Powerband/PowerBandDevice.java | Core BLE device manager & orchestration |
| kph-app-android/.../powerband/command/ | BandCommand, CmdGetDailyData, CmdSetDeviceTime, response parsers |
| kph-app-android/.../powerband/service/ | BatteryService.java, DataService.java, DeviceInformationService.java |
| kph-app-android/app/src/main/res/* | Many assets (avatars, missions, etc.) used by the app |
| kpc-app-android/ | Kid Power Community Android — additional Android app in repo |
| play-store images & assets | Located under each Android app folder (play store images) |
| tools/scripts | (if present) build or helper scripts — search repo for automation |

Tech Stack
----------
- React Native (Hybrid JS + native) <img src="https://img.shields.io/badge/React%20Native-0.50-blue?logo=react" alt="React Native">
- iOS (Swift + CocoaPods): Alamofire, HealthKit <img src="https://img.shields.io/badge/iOS-Swift-orange?logo=swift" alt="iOS">
- Android (Java + Gradle): OkHttp, BLE stacks <img src="https://img.shields.io/badge/Android-Java-brightgreen?logo=android" alt="Android">
- BLE / PowerBand engine (native Android reference) <img src="https://img.shields.io/badge/BLE-PowerBand-blue" alt="BLE">
- Animations: Lottie (RN & iOS) <img src="https://img.shields.io/badge/Lottie-animation-purple" alt="Lottie">
- CodePush (React Native OTA) <img src="https://img.shields.io/badge/CodePush-live-yellow" alt="CodePush">
- Crash & Support: Crashlytics/Fabric, Zendesk SDK

Developer notes & examples
--------------------------
- Example RN fetch in APIManager.js (afg-app-rn) — shows header usage:
  fetch(BASE_URL + "/api/v2/challenges/user/current", { method: "GET", headers: { "X-Access-Token": accessToken } })

- Android OkHttp example (cc-app-android util/APIManager.java):
  Request request = new Request.Builder().url(url).header("x-access-token", accessToken).get().build();

- iOS HealthKit upload flow:
  1. HealthKitManager computes daily summaries
  2. AppDelegate (or other controllers) fetches last sync date from backend using x-access-token header
  3. Uploads results to ActivitySummariesURL with `x-access-token` header via Alamofire

- Testing / local API:
  - Use staging BASE_URL and a valid access token; tokens are passed via X-Access-Token header.
  - To simulate data uploads, call the endpoints with a test user's accessToken.

Future roadmap (table)
----------------------
| Icon | Feature | ETA | Status |
|---:|---|:---:|---|
| 🔒 | Centralize auth SDK & token refresh (unified token management) | Q2 | Planning |
| 📶 | Harden PowerBand reconnection & retry logic (BLE reliability) | Q2 | In progress |
| ⚙️ | CI: Android/iOS builds + lint + unit tests | Q3 | Proposed |
| 🧪 | E2E tests for RN flows (login -> dashboard -> sync) | Q3 | Backlog |
| 📦 | Modularize: PowerBand SDK & auth client as separate packages | Q4 | Backlog |

Contributing
------------
- Pick the subproject you want to work on (afg-app-rn, cc-app-android, kph-app-android, cc-app-ios).
- Standard flow:
  1. Fork repository
  2. Create a topic branch
  3. Run linters and tests for the target platform
  4. Open PR with description and include testing instructions
- Before PR:
  - Remove or do not include keystore files / secrets.
  - Do not commit API keys or production credential files.
  - If you find exposed credentials in this repo, rotate them in your backend and notify the owners.

Security & sensitive data
-------------------------
This repository contains keystore (*.jks) files and some credentials. For public or forked copies:
- Remove or rotate keystores / API keys.
- Use CI secret stores or environment variables for sensitive values.

Where to look for quick tasks
----------------------------
- To add a new RN screen: afg-app-rn/app/screens/
- To debug BLE / PowerBand flows (Android): kph-app-android/.../powerband/PowerBandDevice.java and command classes
- To add an iOS HealthKit change: cc-app-ios/Calorie Cloud/HealthKitManager.swift
- To adjust API endpoints: search APIManager.js (RN) and APIManager.java (Android)

License
-------
MIT License — see LICENSE file.

Acknowledgements
----------------
This monorepo contains work and assets from the UNICEF Kid Power / Calorie Cloud project. Thank you to contributors and maintainers.

If you want, I can:
- Extract a single-subproject README (e.g., afg-app-rn) with step-by-step debugging, environment variables, and common gotchas.
- Produce a small script to call the API endpoints using a test access token for local testing (curl or node script).
