# Requirements Document

## Introduction

This feature is an Android home screen widget that displays Islamic prayer times for the user's current location. The widget shows the five daily prayers (Fajr, Dhuhr, Asr, Maghrib, Isha) along with the next upcoming prayer, and updates automatically based on the device's location and the current date. Users can place the widget on their home screen for at-a-glance access to prayer times without opening an app.

## Glossary

- **Widget**: An Android App Widget that resides on the device home screen and displays prayer time information.
- **Prayer_Time_Calculator**: The component responsible for computing the five daily Islamic prayer times from geographic coordinates, date, and a calculation method.
- **Location_Provider**: The component responsible for obtaining the device's current geographic coordinates (latitude and longitude).
- **Calculation_Method**: A named set of parameters (angles, offsets) used by the Prayer_Time_Calculator to derive prayer times (e.g., Muslim World League, ISNA, Egyptian General Authority).
- **Prayer**: One of the five daily Islamic obligatory prayers: Fajr, Dhuhr, Asr, Maghrib, Isha.
- **Next_Prayer**: The Prayer whose time is closest to and after the current device time.
- **Update_Scheduler**: The component responsible for triggering Widget refresh at defined intervals or events.
- **Settings_Screen**: The in-app Activity where the user configures the Widget's Calculation_Method and display preferences.
- **Permission_Handler**: The component responsible for requesting and verifying Android runtime permissions.

---

## Requirements

### Requirement 1: Display Daily Prayer Times

**User Story:** As a Muslim user, I want to see all five daily prayer times on my home screen widget, so that I can quickly check prayer times without opening an app.

#### Acceptance Criteria

1. THE Widget SHALL display the time for each of the five Prayers (Fajr, Dhuhr, Asr, Maghrib, Isha) in the time format (12-hour or 24-hour) that matches the device's system time format setting, with no independent format override available to the user.
2. THE Widget SHALL display each Prayer's name alongside its computed time.
3. WHEN the current date changes, THE Widget SHALL recompute and display the Prayer times for the new date within 5 minutes of midnight.
4. WHILE a Next_Prayer exists for the current day, THE Widget SHALL highlight the Next_Prayer by applying a distinct background color or bold text style to distinguish it from the other four Prayers.
5. IF the current device time is after the last Prayer (Isha) of the day and before midnight, THEN THE Widget SHALL display no Next_Prayer highlight.

---

### Requirement 2: Compute Prayer Times from Location

**User Story:** As a user, I want prayer times calculated for my current location, so that the times are accurate for where I am.

#### Acceptance Criteria

1. WHEN the Widget is rendered and valid cached coordinates (obtained within the last 24 hours) are available, THE Prayer_Time_Calculator SHALL compute Prayer times using those latitude and longitude coordinates; IF no valid cached coordinates are available, THE Prayer_Time_Calculator SHALL not attempt computation and THE Widget SHALL display the missing-location message defined in Requirement 6.
2. THE Prayer_Time_Calculator SHALL support at least the following Calculation_Methods: Muslim World League, Islamic Society of North America (ISNA), Egyptian General Authority of Survey, and Umm Al-Qura University (Makkah).
3. WHEN a Calculation_Method is selected by the user, THE Prayer_Time_Calculator SHALL use that method's angles and offsets for all subsequent computations.
4. THE Prayer_Time_Calculator SHALL account for the device's local timezone and Daylight Saving Time when formatting computed Prayer times.

---

### Requirement 3: Obtain Device Location

**User Story:** As a user, I want the widget to use my device's location automatically, so that I don't have to enter coordinates manually.

#### Acceptance Criteria

1. WHEN the Widget is added to the home screen for the first time, THE Location_Provider SHALL request the Android `ACCESS_COARSE_LOCATION` permission from the user via the Permission_Handler.
2. WHEN location permission is granted, THE Location_Provider SHALL obtain the device's last known location (if obtained within the last 24 hours) and provide the coordinates to the Prayer_Time_Calculator.
3. IF the Location_Provider cannot obtain a location fix within 30 seconds and cached coordinates exist, THEN THE Widget SHALL display the cached coordinates and indicate the location is cached.
4. IF the Location_Provider cannot obtain a location fix within 30 seconds and no cached coordinates exist, THEN THE Widget SHALL display the missing-location message defined in Requirement 6.
5. WHILE location permission is granted and a new location fix is available, WHEN the new fix differs from the cached coordinates by more than 10 kilometres, THE Location_Provider SHALL trigger a Widget refresh with the updated coordinates.
6. IF location permission is denied by the user, THEN THE Widget SHALL display a message prompting the user to grant location permission via the Settings_Screen.
7. IF location permission is revoked after the Widget was initially set up, THEN THE Location_Provider SHALL cease obtaining new location fixes.
8. WHEN location permission has been revoked, THE Widget SHALL continue to display Prayer times computed from the most recently cached coordinates.

---

### Requirement 4: Automatic Widget Updates

**User Story:** As a user, I want the widget to stay current throughout the day, so that the next prayer highlight is always accurate.

#### Acceptance Criteria

1. THE Update_Scheduler SHALL refresh the Widget at least once every 60 minutes to update the Next_Prayer highlight.
2. WHEN the device time reaches a Prayer's computed time, THE Update_Scheduler SHALL trigger a Widget refresh within 2 minutes to advance the Next_Prayer highlight to the following Prayer.
3. IF the user taps the Widget within the 2-minute window after a Prayer's computed time, THEN that tap SHALL satisfy the Update_Scheduler's required refresh for that prayer transition.
4. WHEN the Widget is tapped by the user, THE Widget SHALL trigger a manual refresh and display updated Prayer times within 5 seconds.
5. WHILE the device is in battery saver mode, THE Update_Scheduler SHALL reduce the periodic refresh interval to once every 120 minutes; event-driven refreshes triggered by a Prayer's computed time being reached SHALL not be affected by battery saver mode.

---

### Requirement 5: User Configuration via Settings Screen

**User Story:** As a user, I want to configure the calculation method and display preferences, so that the widget matches my religious authority and visual preferences.

#### Acceptance Criteria

1. THE Settings_Screen SHALL allow the user to select one Calculation_Method from the supported list defined in Requirement 2.
2. THE Settings_Screen SHALL allow the user to toggle the display of Sunrise time as an optional sixth entry on the Widget; WHERE the user has not yet toggled this setting, the Sunrise entry SHALL be hidden by default.
3. WHEN the user taps the Save button in the Settings_Screen, THE Widget SHALL refresh within 5 seconds to reflect the updated configuration; IF the refresh does not complete within 5 seconds, THEN THE Widget SHALL display a visible error message indicating the refresh failed and a labelled retry button.
4. WHEN the user taps the settings icon on the Widget, THE Settings_Screen SHALL open.
5. WHEN the user opens the host application, THE Settings_Screen SHALL be accessible from the main screen.
6. WHERE the user has not yet configured a Calculation_Method, THE Widget SHALL default to the Muslim World League method.
7. IF the user navigates away from the Settings_Screen without tapping the Save button, THEN any unsaved changes SHALL be discarded and the prior configuration SHALL be preserved.

---

### Requirement 6: Handle Missing or Stale Data

**User Story:** As a user, I want the widget to remain informative even when data cannot be refreshed, so that I always see something useful rather than a blank widget.

#### Acceptance Criteria

1. IF no cached coordinates exist (the device has never successfully obtained a location fix), THEN THE Widget SHALL display the message "Location unavailable — tap to configure" in place of Prayer times.
2. IF the last successful Prayer time computation is 24 hours or more in the past, THEN THE Widget SHALL display a text label showing the age of the last successful computation in whole hours (e.g., "Last updated 25 hours ago") alongside the Prayer times.
3. WHEN the device transitions from no network connectivity to network connectivity restored, THE Update_Scheduler SHALL trigger a location-dependent Widget refresh within 5 minutes.
4. IF the Prayer_Time_Calculator receives a coordinate where latitude is outside the range −90 to 90 degrees or longitude is outside the range −180 to 180 degrees, THEN THE Prayer_Time_Calculator SHALL return an error without computing Prayer times.
5. WHEN the Prayer_Time_Calculator returns an invalid-coordinate error, THE Widget SHALL display "Invalid location data", taking priority over any other missing-location message.

---

### Requirement 7: Widget Resize Support

**User Story:** As a user, I want to resize the widget on my home screen, so that I can choose how much space it occupies.

#### Acceptance Criteria

1. WHILE the Widget is sized at 2×2 home screen cells, THE Widget SHALL display only the Next_Prayer name and time.
2. WHILE the Widget is sized at 4×2 home screen cells, THE Widget SHALL display all five Prayer names and times, plus the optional Sunrise entry if enabled.
3. WHILE the Widget is sized between 2×2 and 4×2 cells (intermediate sizes), THE Widget SHALL display the Next_Prayer name and time plus as many additional Prayers as fit within the available space, in chronological order.
4. THE Widget SHALL support a maximum size of 4×4 home screen cells, using the same layout as the 4×2 size.
5. WHEN the Widget is resized by the user, THE Widget SHALL rerender its layout to match the new cell dimensions within 1 second and without requiring a device restart.
