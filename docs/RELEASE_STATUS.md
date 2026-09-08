# Release status

Updated: 2026-09-09

This file records evidence that can be reproduced from the repository. It does
not replace device, Play Console, Firebase Console, signing, or closed-test
verification.

## Current code and build gates

- Application ID remains `com.healthmetrics.tracker`.
- Latest completed audit commit is tracked in Git history; verify the SHA with
  `git log -1 --oneline` before creating a release artifact.
- Required local gates are `test`, `lintRelease`, `assembleDebug`,
  `assembleRelease`, and `bundleRelease`.
- Release signing is intentionally credential-gated through
  `RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, and
  `RELEASE_KEY_PASSWORD`; no key or password is stored in this repository.

## Audit fixes now covered by code/tests

- P1 privacy and medical/data correctness fixes are complete; multi-profile
  switching remains disabled until records are profile-scoped.
- P2 history details parsing, food-log day boundaries, BMR ranges, WHR wording,
  BP wall-clock reminders, VO2 false-precision surfaces, hydration heuristic
  disclosure, and 48dp targets are implemented.
- A Room `MigrationTestHelper` test covers the checked-in 15→16 migration and
  verifies row preservation plus the `step_history` table.

## Open runtime or owner gates

These cannot be proven by a Windows unit/build run:

1. Confirm the oldest database version ever distributed to users. If a real
   release predates version 13, restore that database and add schema fixtures
   plus migrations through version 16 before launch.
2. Run the migration test on a connected emulator/device; the repository only
   has schema fixtures for versions 15 and 16.
3. Run cold-start and macrobenchmark measurements with Firebase/App Check
   enabled in a release-like build; no startup performance number is claimed.
4. Exercise BP reminders across reboot, `TIME_SET`, timezone and DST changes,
   notification denial, and OEM battery restrictions.
5. Run Compose/accessibility checks on small and large screens, 1.5–2.0x font
   scale, TalkBack, dark/light mode, and API 26/33/34/35/36 devices.
6. Validate Health Connect aggregation with multiple data sources, denied
   permissions, unsupported providers, and process death.
7. Complete Play Console/Firebase owner tasks: release signing, App Check
   production registration, privacy/data-safety declarations, closed testing,
   store listing, and crash/ANR monitoring.
