# Implementation Plan: Islamic Prayer Times Widget

## Overview

Implement an Android home screen App Widget (Jetpack Glance, Kotlin, API 26+) that displays the five daily Islamic prayer times computed from the device's current location. The implementation follows a clean layered architecture: domain models and interfaces first, then data/platform implementations, then use-case orchestration, then the widget UI, and finally scheduling and settings.

## Tasks

- [x] 1. Set up project structure, dependencies, and core domain models
  - Convert the existing Gradle project to an Android application module (apply `com.android.application` plugin, set `compileSdk`/`minSdk 26`, add Kotlin plugin)
  - Add dependencies: Jetpack Glance, Adhan-Java, Google Play Services Location, WorkManager, Kotest, JUnit 5, MockK
  - Create the package hierarchy: `domain`, `data`, `usecase`, `scheduling`, `ui.widget`, `ui.settings`
  - Define `Prayer` enum, `PrayerEntry` data class, `Coordinates` data class, `WidgetState` data class, `CalculationMethod` enum with `toAdhanParameters()`, `UpdateResult` sealed class
  - Define all interfaces: `PrayerTimeCalculator`, `NextPrayerCalculator`, `TimeFormatter`, `LocationProvider`, `CoordinatesRepository`, `SettingsRepository`, `PrayerAlarmScheduler`
  - Define `PrayerTimesResult`, `LocationResult`, `CachedCoordinates` sealed/data classes
  - _Requirements: 1.1, 1.2, 2.1, 2.2, 2.3, 3.1_

- [ ] 2. Implement domain layer — prayer time calculation and next-prayer logic
  - [ ] 2.1 Implement `AdhanPrayerTimeCalculator` (concrete `PrayerTimeCalculator`)
    - Validate coordinates (lat ∈ [-90,90], lon ∈ [-180,180]); return `InvalidCoordinates` on failure
    - Delegate to Adhan-Java `PrayerTimes` with the mapped `CalculationParameters`
    - Return `PrayerTimesResult.Success` with five `PrayerEntry` items in chronological order plus optional `sunrise`
    - _Requirements: 1.1, 1.2, 2.1, 2.2, 2.3, 6.4, 6.5_

  - [ ]* 2.2 Write property test for `AdhanPrayerTimeCalculator` — Property 1
    - **Property 1: Prayer time completeness and chronological ordering**
    - **Validates: Requirements 1.1, 1.2, 2.1, 2.2**
    - Use `Arb.double(-90.0, 90.0)` × `Arb.double(-180.0, 180.0)` × `Arb.enum<CalculationMethod>()`; assert exactly 5 entries returned in strictly increasing order

  - [ ]* 2.3 Write property test for `AdhanPrayerTimeCalculator` — Property 2
    - **Property 2: Coordinate validation rejects out-of-range inputs**
    - **Validates: Requirements 6.4, 6.5**
    - Generate out-of-range lat or lon values; assert result is always `InvalidCoordinates`

  - [ ]* 2.4 Write property test for `AdhanPrayerTimeCalculator` — Property 7
    - **Property 7: Distinct calculation methods produce distinct prayer times**
    - **Validates: Requirements 2.2, 2.3**
    - Generate pairs of distinct `CalculationMethod` values and valid coordinates; assert at least one prayer time differs

  - [-] 2.5 Implement `DefaultNextPrayerCalculator` (concrete `NextPrayerCalculator`)
    - Iterate sorted `PrayerEntry` list; return index of first entry whose `time > now`, or `null` if none
    - _Requirements: 1.4, 1.5_

  - [ ]* 2.6 Write property test for `DefaultNextPrayerCalculator` — Property 3
    - **Property 3: Next prayer identification correctness**
    - **Validates: Requirements 1.4, 1.5**
    - Generate sorted `Arb.list(Arb.instant(), 1..5)` × `Arb.instant()`; assert returned index is the smallest index with `time > now`, or `null` when all prayers have passed

  - [ ] 2.7 Implement `SystemTimeFormatter` (concrete `TimeFormatter`)
    - Use `DateTimeFormatter` / `DateFormat.getTimeInstance` respecting `DateFormat.is24HourFormat(context)`
    - Return HH:mm for 24-hour devices; h:mm a for 12-hour devices
    - _Requirements: 1.1_

  - [ ]* 2.8 Write property test for `SystemTimeFormatter` — Property 6
    - **Property 6: Time format respects device system setting**
    - **Validates: Requirements 1.1**
    - Generate `Arb.instant()` × `Arb.boolean()` (is24Hour flag); assert presence/absence of AM/PM suffix

- [~] 3. Checkpoint — domain layer complete
  - Ensure all domain unit and property tests pass, ask the user if questions arise.

- [ ] 4. Implement data layer — repositories and location provider
  - [~] 4.1 Implement `SharedPreferencesCoordinatesRepository` (concrete `CoordinatesRepository`)
    - Persist/load `pref_lat`, `pref_lon`, `pref_location_ts` via `SharedPreferences`
    - Implement `isFresh(maxAgeHours)` comparing stored timestamp to `Instant.now()`
    - _Requirements: 2.1, 3.2, 3.3, 3.8_

  - [ ]* 4.2 Write unit tests for `SharedPreferencesCoordinatesRepository`
    - Test save/load round-trip, freshness check at exactly 24 h boundary, null return when never saved
    - _Requirements: 2.1, 3.2_

  - [~] 4.3 Implement `SharedPreferencesSettingsRepository` (concrete `SettingsRepository`)
    - Persist/load `pref_calc_method` (default `MUSLIM_WORLD_LEAGUE`) and `pref_show_sunrise` (default `false`)
    - _Requirements: 2.3, 5.1, 5.2, 5.6_

  - [ ]* 4.4 Write property test for `SharedPreferencesSettingsRepository` — Property 4
    - **Property 4: Calculation method settings round-trip**
    - **Validates: Requirements 2.3, 5.1, 5.6**
    - Generate `Arb.enum<CalculationMethod>()`; save then read back; assert equality

  - [~] 4.5 Implement `FusedLocationProvider` (concrete `LocationProvider`)
    - Wrap `FusedLocationProviderClient.lastLocation` with a 30-second coroutine timeout
    - Return `LocationResult.PermissionDenied` when `ACCESS_COARSE_LOCATION` is not granted
    - Return `LocationResult.Timeout` on timeout; `LocationResult.Unavailable` on null result
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.6, 3.7_

  - [ ]* 4.6 Write unit tests for `FusedLocationProvider`
    - Mock `FusedLocationProviderClient`; test permission-denied path, timeout path, success path
    - _Requirements: 3.1, 3.3, 3.4_

- [~] 5. Checkpoint — data layer complete
  - Ensure all data-layer tests pass, ask the user if questions arise.

- [ ] 6. Implement use-case orchestration
  - [~] 6.1 Implement `WidgetUpdateUseCase`
    - Read cached coordinates from `CoordinatesRepository`; if stale/absent, call `LocationProvider`
    - On `LocationResult.PermissionDenied` → return `UpdateResult.MissingLocation`
    - On `LocationResult.Timeout` with cache → use cache, set `isLocationCached = true`
    - On `LocationResult.Timeout` without cache → return `UpdateResult.MissingLocation`
    - Validate coordinates; on `InvalidCoordinates` → return `UpdateResult.InvalidCoordinates`
    - Call `PrayerTimeCalculator.calculate()`; annotate each `PrayerEntry.isNext` via `NextPrayerCalculator`
    - Compute `staleDataAgeHours` from `pref_last_compute_ts`; set in `WidgetState` if ≥ 24 h
    - Call `PrayerAlarmScheduler.scheduleNext()` on success
    - Return `UpdateResult.Success` or `UpdateResult.StaleData`
    - _Requirements: 1.4, 1.5, 2.1, 3.2, 3.3, 3.4, 6.1, 6.2, 6.4, 6.5_

  - [ ]* 6.2 Write unit tests for `WidgetUpdateUseCase`
    - Use MockK to mock all dependencies; exercise each `UpdateResult` branch
    - Test stale-data path, missing-location path, invalid-coordinates path, success path
    - _Requirements: 6.1, 6.2, 6.4, 6.5_

  - [ ]* 6.3 Write property test for stale data age — Property 5
    - **Property 5: Stale data age label accuracy**
    - **Validates: Requirements 6.2**
    - Generate `Arb.instant()` pairs where difference ≥ 24 h; assert `staleDataAgeHours == floor(diff.toHours())`

- [ ] 7. Implement scheduling — AlarmManager and WorkManager
  - [~] 7.1 Implement `AlarmManagerPrayerAlarmScheduler` (concrete `PrayerAlarmScheduler`)
    - For each `PrayerEntry` in the list, schedule an exact alarm at `entry.time` using `setExactAndAllowWhileIdle`
    - On API 31+ check `canScheduleExactAlarms()`; fall back to `setAndAllowWhileIdle` and log a warning if denied
    - Implement `cancelAll()` to cancel all pending `PendingIntent`s
    - Broadcast `ACTION_PRAYER_ALARM` to `WidgetReceiver` on alarm fire
    - _Requirements: 4.2, 4.3_

  - [ ]* 7.2 Write unit tests for `AlarmManagerPrayerAlarmScheduler`
    - Mock `AlarmManager`; verify `setExactAndAllowWhileIdle` is called for each prayer; verify `cancelAll` cancels all intents
    - _Requirements: 4.2_

  - [~] 7.3 Implement `PeriodicRefreshWorker` (`CoroutineWorker`)
    - Invoke `WidgetUpdateUseCase` inside `doWork()`
    - Detect `PowerManager.isPowerSaveMode`; re-enqueue with 60-min or 120-min interval accordingly
    - Return `Result.success()` on `UpdateResult.Success`/`StaleData`; `Result.retry()` on failure
    - _Requirements: 4.1, 4.5_

  - [ ]* 7.4 Write unit tests for `PeriodicRefreshWorker`
    - Use `TestListenableWorkerBuilder`; verify correct interval selection in battery-saver vs normal mode
    - _Requirements: 4.1, 4.5_

- [~] 8. Checkpoint — scheduling layer complete
  - Ensure all scheduling tests pass, ask the user if questions arise.

- [ ] 9. Implement widget UI — Glance composable and receiver
  - [~] 9.1 Implement `PrayerTimesWidget` (`GlanceAppWidget`)
    - Define `GlanceStateDefinition` backed by `WidgetState` (serialized via `DataStore`)
    - In `provideGlance`, read `LocalSize` to select layout variant:
      - Compact (≤ 2×2 cells): render only next prayer name + time
      - Intermediate (between 2×2 and 4×2): render next prayer + as many chronological prayers as fit
      - Full (≥ 4×2 or 4×4): render all five prayers (+ Sunrise if enabled), highlight next prayer
    - Apply distinct background/bold style to the `isNext` entry
    - Render `WidgetState.errorMessage` when non-null (highest priority)
    - Render stale-data label when `staleDataAgeHours != null`
    - _Requirements: 1.1, 1.2, 1.4, 1.5, 5.2, 6.1, 6.2, 6.5, 7.1, 7.2, 7.3, 7.4_

  - [ ]* 9.2 Write property test for intermediate widget layout — Property 9
    - **Property 9: Intermediate widget size shows prayers in chronological order**
    - **Validates: Requirements 7.3**
    - Generate `Arb.int(3..7)` × `Arb.int(2..3)` cell dimensions × `Arb.list(PrayerEntry, 5)`; assert displayed list starts with next prayer and is in chronological order with count ∈ [1,5]

  - [~] 9.3 Implement `WidgetReceiver` (`GlanceAppWidgetReceiver`)
    - Override `onReceive` to handle `ACTION_APPWIDGET_UPDATE`, `ACTION_APPWIDGET_ENABLED`, and `ACTION_PRAYER_ALARM`
    - Launch a coroutine to invoke `WidgetUpdateUseCase` for each trigger
    - Wire tap on widget to send `ACTION_APPWIDGET_UPDATE` broadcast (manual refresh)
    - Wire tap on settings icon to open `SettingsActivity` via `PendingIntent`
    - _Requirements: 4.3, 4.4, 5.4_

  - [~] 9.4 Register `PrayerTimesWidget` and `WidgetReceiver` in `AndroidManifest.xml`
    - Add `<receiver>` for `WidgetReceiver` with `BIND_APPWIDGET` permission and `appwidget-provider` metadata
    - Create `res/xml/prayer_times_widget_info.xml` with `minWidth`, `minHeight`, `resizeMode`, `updatePeriodMillis=0`
    - Add `ACCESS_COARSE_LOCATION`, `RECEIVE_BOOT_COMPLETED`, `SCHEDULE_EXACT_ALARM` (API 31+) permissions
    - _Requirements: 3.1, 4.2, 7.1, 7.4, 7.5_

- [~] 10. Checkpoint — widget UI complete
  - Ensure widget renders correctly in all size variants; ask the user if questions arise.

- [ ] 11. Implement Settings screen
  - [~] 11.1 Implement `SettingsActivity` with Compose UI
    - Host a `setContent { }` block with a `SettingsScreen` composable
    - Display a dropdown/radio group for `CalculationMethod` selection (pre-populated from `SettingsRepository`)
    - Display a toggle switch for "Show Sunrise" (pre-populated from `SettingsRepository`)
    - On Save: write to `SettingsRepository`, send `ACTION_APPWIDGET_UPDATE` broadcast, await result with 5-second timeout
    - On timeout: show inline error banner and a labelled "Retry" button
    - On back/navigate-away without Save: discard changes
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7_

  - [ ]* 11.2 Write unit tests for `SettingsActivity` / `SettingsScreen`
    - Use Compose test rules; verify dropdown shows all four methods, toggle reflects saved state, Save triggers broadcast, unsaved changes are discarded on back
    - _Requirements: 5.1, 5.2, 5.3, 5.7_

- [ ] 12. Implement connectivity-triggered refresh
  - [~] 12.1 Implement `ConnectivityChangeReceiver` (`BroadcastReceiver`)
    - Register for `CONNECTIVITY_ACTION` / `NetworkCallback` (API 26+)
    - On connectivity restored, enqueue a one-time `WorkManager` task that invokes `WidgetUpdateUseCase`
    - _Requirements: 6.3_

  - [ ]* 12.2 Write unit tests for `ConnectivityChangeReceiver`
    - Mock `WorkManager`; verify one-time work is enqueued on connectivity-restored event and not on connectivity-lost event
    - _Requirements: 6.3_

- [ ] 13. Wire application entry point and dependency injection
  - [~] 13.1 Create `PrayerTimesApplication` (`Application` subclass)
    - Instantiate and hold singleton instances of all repositories, use cases, and schedulers (manual DI or Hilt)
    - Register `PeriodicRefreshWorker` with WorkManager on app start (unique periodic work, `KEEP` policy)
    - Register `ConnectivityChangeReceiver` if using dynamic registration
    - _Requirements: 4.1, 6.3_

  - [ ]* 13.2 Write integration smoke test for application wiring
    - Verify `PeriodicRefreshWorker` is enqueued after `Application.onCreate()`
    - _Requirements: 4.1_

- [~] 14. Final checkpoint — full integration
  - Ensure all unit tests, property tests, and integration smoke tests pass; ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for a faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation at each architectural layer
- Property tests validate universal correctness properties (Properties 1–9 from the design document)
- Unit tests validate specific examples, edge cases, and error conditions
- The `*` tasks MUST NOT be implemented automatically — they are opt-in

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["2.1", "2.5", "2.7"] },
    { "id": 1, "tasks": ["2.2", "2.3", "2.4", "2.6", "2.8", "4.1", "4.3", "4.5"] },
    { "id": 2, "tasks": ["4.2", "4.4", "4.6", "6.1"] },
    { "id": 3, "tasks": ["6.2", "6.3", "7.1", "7.3"] },
    { "id": 4, "tasks": ["7.2", "7.4", "9.1", "9.3"] },
    { "id": 5, "tasks": ["9.2", "9.4", "11.1", "12.1"] },
    { "id": 6, "tasks": ["11.2", "12.2", "13.1"] },
    { "id": 7, "tasks": ["13.2"] }
  ]
}
```
