# Update on WearOS Application Upgrade from API 28 to 33

## New features
- Steps counting
- Audio recording
- Support for inapp dialogs and native notifications


## Updates
Transitioning from API level 28 to 33 was a substantial task that required more effort and time than initially anticipated. Below are the key changes and improvements implemented:

**1. Notifications and Dialog System Overhaul**
- Completely redesigned the notification framework to align with the new API standards.
- Rebuilt the dialog system to improve user interaction and comply with updated UI guidelines.

**2. Permissions Management**
- Updated the permission requests to match the new runtime permission model introduced in API 33.
- Removed deprecated storage permissions and ensured compliance with the latest privacy policies.

**3. Sensor Data Handling**
- Revised the methods for reading sensor data due to changes in the sensor APIs.
- Enhanced accuracy and efficiency in sensor readings to improve overall app performance.

**4. Code Refactoring and Modularization**
- Refactored a total of 81 files, almost a complete overhaul of the codebase.
- Improved code modularity for better maintainability and scalability in future updates.

**5. User Interface and Experience Enhancements**
- Improved user input handling and default settings in various activities.

**6. Workflow and Timing Adjustments**
- Updated workflow timings and intervals for better synchronization and performance.
- Implemented native notifications and separated them from dialog handling for clarity.

**7. Audio and Recording Improvements**
- Fixed issues with audio playback during dialog displays.
- Refactored the audio recorder to streamline functionality and remove unnecessary messages.

**8. Installation and Release Updates**
- Enhanced installation instructions for better user guidance.
- Prepared and released version 2 incorporating all recent changes and improvements.

## Challenges Faced
The upgrade introduced significant API changes and deprecations, necessitating a near-complete rewrite of several core components. Adapting to new privacy policies required meticulous adjustments in permissions and data handling processes.

