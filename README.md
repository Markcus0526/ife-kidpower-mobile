# IFE-KIDPOWER-MOBILE

<img src="https://img.shields.io/badge/license-MIT-green" alt="license">
<img src="https://img.shields.io/badge/monorepo-multi--platform-blue" alt="monorepo">
<img src="https://img.shields.io/badge/status-maintained-yellow" alt="status">

One repository containing multiple mobile apps and components used by the Kid Power / Calorie Cloud family of apps:
- A React Native app (Active for Good / AFG)
- Native Android apps (Calorie Cloud, Kid Power Home, Kid Power Community)
- Native iOS app (Calorie Cloud)
- Platform integrations: Bluetooth PowerBand engine, HealthKit, Google Fit, Lottie animations, CodePush

This README gives a quick developer guide, highlights the Kid Power Band engine, and documents how authentication works across the codebase.

Table of contents
- Quick start
  - Prerequisites
  - React Native (AFG)
  - Android (native)
  - iOS (native)
- Kid Power Band engine (what, where, how)
- User authentication (how tokens are used and where)
- Configuration & Environment
- Developer notes (BLE, HealthKit, Google Fit, CodePush)
- Tech Stack
- Future Table
- Contributing
- License

---

## Quick start

Important: this is a large monorepo with multiple apps. Pick the app you need to run/develop and follow the relevant section.

Prerequisites
- Node.js (LTS recommended)
- npm or yarn
- Android SDK (for Android builds)
- Xcode and CocoaPods (for iOS builds)
- Java 8 / JDK compatible with Android Gradle plugin used in each app
- For RN debugging: react-native CLI and a running Metro packager

### React Native app (afg-app-rn)
This is the React Native portion used by Active for Good.

1. Install dependencies
   ```
   cd afg-app-rn
   npm install
   ```
   or
   ```
   yarn
   ```

2. Start Metro
   ```
   npm start
   # or
   yarn start
   ```

3. Run on simulator/device
   - Android: open the Android subproject in Android Studio (or use gradlew), or run react-native run-android from the repo root (ensure correct working directory).
   - iOS: open the workspace after running Pod install (see iOS section) and run from Xcode or react-native run-ios.

Notes:
- Entry point: afg-app-rn/index.js (registers Onboarding, Dashboard, Tracker components)
- API calls: afg-app-rn/app/api/APIManager.js uses endpoints and expects X-Access-Token in headers.

### Android native apps (cc-app-android, kph-app-android, kpc-app-android)
The repo contains multiple Android applications (Calorie Cloud variants and Kid Power apps). Each is a standard Android Gradle project.

Build (example for cc-app-android)
1. Open in Android Studio or use Gradle wrapper:
   ```
   ./cc-app-android/gradlew assembleDebug
   ```
2. Install on device:
   ```
   adb install -r cc-app-android/app/build/outputs/apk/debug/app-debug.apk
   ```

Notes:
- Top-level Gradle config: cc-app-android/build.gradle
- Manifest and permissions: cc-app-android/app/src/main/AndroidManifest.xml
- For KPH (Kid Power Home) the BLE/PowerBand engine lives under: kph-app-android/app/src/main/java/.../powerband (see "Kid Power Band engine" below)

### iOS native app (cc-app-ios)
1. Install CocoaPods for the iOS target(s):
   ```
   cd cc-app-ios
   pod install
   ```
2. Open the generated workspace:
   ```
   open "Calorie Cloud.xcworkspace"
   ```
3. Build & Run in Xcode (choose target scheme)

Notes:
- Podfile at cc-app-ios/Podfile lists React, lottie, CodePush and native deps.
- AppDelegate.swift contains native initialization (React bridge, CodePush bundle selection, HealthKit/Help center initialization).

---

## Kid Power Band engine
The Kid Power Band is a BLE device with a local engine implemented primarily in the Android Kid Power app. The code for the PowerBand engine provides:
- BLE scanning/connection and GATT
- Command and response parsing for the band protocol
- Higher-level services to request daily summary, firmware info, device time, etc.
- Bridging so the native app can send sync packets to the backend

Where to look
- kph-app-android/app/src/main/java/.../powerband
  - PowerBandDevice.java
  - powerband/command/* — BandCommand, BandCommandResponse, Cmd* classes (CmdGetDailyData, CmdDeviceTimeGet, CmdSetDeviceTime, etc.)
  - powerband/service/* — BatteryService.java, BleService.java, DataService.java, DeviceInformationService.java
  - powerband/command/Parser classes (e.g., BandCommandResponseParser.java)

How it works (high-level)
1. BleScanner finds devices and uses PowerBandDevice wrapper to manage state.
2. Commands (BandCommand subclasses) are built and sent to the band via the BLE write characteristic.
3. Responses are parsed by the BandCommandResponseParser and delivered to command callbacks or services.
4. DataService aggregates sensor summaries (daily summaries) and uploads via the app's REST endpoints (see APIManager patterns).
5. The Android code includes linking helpers and sync helpers under sync/ and powerband/command/Cmd* classes to coordinate upload tasks.

If you are integrating a new platform:
- You will need to implement the BLE command/response flow and mirror the command definitions to your target platform.
- Pay attention to the command parsers: they define binary layout and framing; reusing these exactly is required to interoperate with the band.

---

## User authentication (overview & details)
Authentication across the apps is token-based. The common pattern:
- Apps obtain an access token after login and supply it to backend requests via an HTTP header: X-Access-Token
- The React Native API layer (afg-app-rn/app/api/APIManager.js) uses this header for GETs:
  - Example: fetch(BASE_URL + APIManager.CURRENT_CHALLENGES, { headers: { "X-Access-Token": accessToken } })
- Native Android code typically sends the same X-Access-Token header (see network / APIManager classes in the Android subprojects)
- iOS (Swift) saves a serialized User and retrieves it via CCStatics.getSavedUser() — AppDelegate and other controllers use the saved user object and its accessToken when making Alamofire requests.

Where to look
- React Native: afg-app-rn/app/api/APIManager.js — shows endpoints and header usage (X-Access-Token)
- iOS: cc-app-ios/Calorie Cloud/AppDelegate.swift and CCStatics.getSavedUser() usages; HealthKit sync code demonstrates authenticated requests using savedUser.accessToken
- Android: cc-app-android and kph-app-android have LoginActivity.java and APIManager-like classes in server/apimanage which set the token in request headers

Practical notes for developers
- To test API calls locally, set BASE_URL to your staging API and use a valid access token (tokens are bearer-like but in the custom header X-Access-Token).
- When performing end-to-end testing, make sure your test user exists on the backend and has valid accessToken and userId.
- The apps persist users locally (Android: SharedPreferences or serialized file, iOS: CCStatics). On React Native, the app passes tokens as props to components (see Dashboard / Onboarding props usage).

Security note (important)
- This repository currently contains keystore (*.jks) files in Android subprojects and API keys in Podfile / manifests. These are sensitive assets; in production workflows:
  - Keep keystores and sensitive keys out of source control.
  - Use secure secrets management or CI-based protected variables.
  - Replace exposed keys if this repository has been made public.

---

## Configuration & environment
Common configuration items you will need:
- BASE_URL — backend base url, used by APIManager implementations
- userId and accessToken — obtained after login; passed to API endpoints (X-Access-Token)
- Android keystores (for production signing) — do not commit in public repos; they exist in this repo for convenience
- iOS: Pods and CodePush keys (in Podfile references and CCStatics)

Example (React Native) environment:
- Set the base URL in your native config or pass it to components from native bridge. The RN APIManager expects BASE_URL + endpoints.

API endpoints (React Native sample)
- CURRENT_CHALLENGES = /api/v2/challenges/user/current
- UPCOMING_CHALLENGES = /api/v2/challenges/user/upcoming
- PREVIOUS_CHALLENGES = /api/v2/challenges/user/completed
- CHALLENGE_SUMMARY = /api/v2/userSummary/challenge/:challengeId/user/:userId
- LAST_SYNC_DATE_DEVICE = /api/v2/getLastSyncDate/user/:userId
- LATEST_CHALLENGE = /api/v2/challenges/latest/user/:userId

Always include header:
- X-Access-Token: <accessToken>

---

## Developer notes

Bluetooth & PowerBand
- The engine expects binary command packets and a deterministic response parser. See kph-app-android powerband code for the canonical implementation.
- On Android, BleScanner and BlePeripheral classes manage scanning and connection lifecycle.
- On iOS you will need to implement a compatible BLE layer if you want to perform the same low-level PowerBand flows.

HealthKit / Google Fit
- iOS uses HealthKitManager.swift to read active calories and step counts, and uploads via ActivitySummariesURL (see AppDelegate and getCalorieData/getStepData).
- Android integrates Google Fit (play-services-fitness) and uses a wrapper activity GoogleFitWrapperActivity to authorize and sync.

Animations & OTA
- Lottie is used for animations (lottie-react-native + lottie-ios).
- CodePush is included for JS bundle hot updates in the React Native app.

Crash & Support
- Crashlytics/Fabric and Zendesk SDK are integrated for crash reporting and support.

---

## Tech Stack

- React Native
  <img src="https://img.shields.io/badge/React%20Native-0.50-blue?logo=react" alt="React Native">
- Node / Package management
  <img src="https://img.shields.io/badge/Node.js-%E2%98%A5-339933?logo=node.js" alt="Node.js">
- Android (Java, Gradle)
  <img src="https://img.shields.io/badge/Android-Java-brightgreen?logo=android" alt="Android">
- iOS (Swift, CocoaPods)
  <img src="https://img.shields.io/badge/iOS-Swift-orange?logo=swift" alt="iOS">
- BLE / Bluetooth Low Energy
  <img src="https://img.shields.io/badge/BLE-Bluetooth-blue" alt="BLE">
- HealthKit & Google Fit
  <img src="https://img.shields.io/badge/HealthKit-Apple-red" alt="HealthKit">
  <img src="https://img.shields.io/badge/Google%20Fit-Google-lightgrey" alt="Google Fit">
- Lottie (animations)
  <img src="https://img.shields.io/badge/Lottie-animation-purple" alt="Lottie">
- CodePush (RN updates)
  <img src="https://img.shields.io/badge/CodePush-live-yellow" alt="CodePush">

(Icons are provided as inline badges for quick recognition.)

---

## Future Table (roadmap / planned improvements)

| Icon | Feature | ETA | Status |
|---|---:|:---:|:---|
| <img src="https://img.shields.io/badge/🔒-Auth-blue" alt="auth"> | Centralize auth SDK & token refresh | Q2 | Planning |
| <img src="https://img.shields.io/badge/📶-BLE-improvement-blue" alt="ble"> | Harden PowerBand reconnection & retry logic | Q2 | In progress |
| <img src="https://img.shields.io/badge/⚙️-CI-green" alt="ci"> | Add CI pipelines for Android/iOS unit and lint checks | Q3 | Proposed |
| <img src="https://img.shields.io/badge/🧪-tests-red" alt="tests"> | Add E2E tests for RN flows (login/dashboard) | Q3 | Backlog |
| <img src="https://img.shields.io/badge/📦-modularize-orange" alt="modularize"> | Split reusable modules (PowerBand SDK, auth lib) into packages | Q4 | Backlog |

Legend: ETA = target quarter, Status = current planning stage.

---

## Contributing
- Check the subproject you want to work on (afg-app-rn, cc-app-android, kph-app-android, cc-app-ios).
- Follow each platform's standard contribution flow:
  - Fork, create topic branch, run tests and linters, push changes, open PR.
- Before submitting PR:
  - Remove or redact any secrets/keystores from patches.
  - Ensure no hard-coded API keys or passwords are committed.
  - Provide a short description and steps to reproduce/test your changes.

Security & private data
- This repo contains keystore files and API keys in some subfolders. If you are forking or publishing, rotate and remove these before public sharing.
- Manage secrets using environment variables or CI secret stores.

---

## Where to find things (important files)
- React Native app: afg-app-rn/
  - Entry: index.js
  - API: app/api/APIManager.js
  - Screens: app/screens/Onboarding.js, Dashboard.js
- iOS app: cc-app-ios/
  - AppDelegate: Calorie Cloud/AppDelegate.swift
  - Podfile: cc-app-ios/Podfile
- Android apps:
  - Calorie Cloud: cc-app-android/
  - Kid Power Home: kph-app-android/
  - PowerBand engine: kph-app-android/app/src/main/java/.../powerband/
- License: LICENSE (MIT)

---

## License
This project is licensed under the MIT License — see the LICENSE file for details.

---

If you'd like, I can:
- Extract a minimal README for a single subproject (e.g., afd-app-rn) with more detailed RN build, debugging and troubleshooting steps.
- Add a small authentication demo (script) showing how to call one of the API endpoints with X-Access-Token for local testing.
