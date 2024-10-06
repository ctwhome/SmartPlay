# SmartPlay Tests

This directory contains unit tests for the SmartPlay application, focusing on the SettingsActivity and RecordingActivity.

## Test Files

1. `SettingsActivityTest.kt`: Tests for the SettingsActivity, including default settings and changing settings.
2. `RecordingActivityTest.kt`: Tests for the RecordingActivity, including starting, stopping, pausing, and resuming recording.

## Running the Tests

To run the tests, follow these steps:

1. Open a terminal and navigate to the root directory of the SmartPlay project.

2. Run the following command to execute all tests:
   ```
   ./gradlew test
   ```

3. To run tests for a specific class, use:
   ```
   ./gradlew test --tests "com.example.smartplay.tests.SettingsActivityTest"
   ```
   or
   ```
   ./gradlew test --tests "com.example.smartplay.tests.RecordingActivityTest"
   ```

4. After running the tests, you can find the test results in the following directory:
   ```
   app/build/reports/tests/testDebugUnitTest/index.html
   ```
   Open this file in a web browser to view detailed test results.

## Adding More Tests

To add more tests:

1. Create a new test file in this directory with the naming convention `*Test.kt`.
2. Write your test cases using JUnit4 annotations (@Test, @Before, etc.).
3. Use Robolectric for Android-specific testing and Mockito for mocking dependencies.

Remember to update the `app/build.gradle` file if you need to add any new testing dependencies.

## Continuous Integration

Consider setting up a CI/CD pipeline (e.g., GitHub Actions) to automatically run these tests on every push or pull request to ensure code quality and catch regressions early.