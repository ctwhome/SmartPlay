# SmartPlay Testing Guide

This document provides comprehensive information about testing the SmartPlay application.

## Test Structure

All tests are located in the `app/src/test/java/com/example/smartplay/` directory, organized by functionality:

```
app/src/test/java/com/example/smartplay/
├── RecordingActivityTest.kt
├── SettingsActivityTest.kt
├── recording/
│   ├── FileUtilsTest.kt
│   └── DataRecorderTest.kt
└── workflow/
    ├── WorkflowManagerTest.kt
    ├── WorkflowTypesTest.kt
    └── notifications/
        └── NotificationManagerTest.kt
```

## Test Coverage

### 1. FileUtilsTest
**Location:** `app/src/test/java/com/example/smartplay/recording/FileUtilsTest.kt`

Tests file operations and JSON parsing functionality:
- Reading workflow files from internal and external storage
- Parsing JSON workflow configurations
- Extracting workflow names from JSON content
- Handling invalid JSON gracefully
- Managing missing or malformed workflow data

**Key Test Cases:**
- `testReadFileFromAppSpecificDirectory_FileExistsInInternal()`
- `testGetWorkflowNamesFromContent_ValidJSON()`
- `testGetWorkflowNamesFromContent_InvalidJSON()`
- `testGetWorkflowNamesFromContent_MixedValidAndInvalidEntries()`

### 2. DataRecorderTest
**Location:** `app/src/test/java/com/example/smartplay/recording/DataRecorderTest.kt`

Tests CSV data recording functionality:
- Creating sensor, bluetooth, and question CSV files
- Writing sensor data with correct formatting
- Writing bluetooth device information
- Writing question responses
- Respecting user preferences for enabled sensors
- Handling uninitialized recorder gracefully

**Key Test Cases:**
- `testInitializeFiles_CreatesAllFiles()`
- `testWriteSensorData_WritesCorrectFormat()`
- `testWriteBluetoothData_WritesCorrectFormat()`
- `testWriteQuestionData_WritesCorrectFormat()`
- `testSensorHeader_RespectsPreferences()`

### 3. WorkflowManagerTest
**Location:** `app/src/test/java/com/example/smartplay/workflow/WorkflowManagerTest.kt`

Tests workflow management functionality:
- Initializing workflows from JSON configuration
- Selecting specific workflows by name
- Handling invalid JSON gracefully
- Managing multiple workflows
- Parsing complex question structures

**Key Test Cases:**
- `testInitializeWorkflow_ValidJSON_ReturnsWorkflow()`
- `testInitializeWorkflow_InvalidJSON_ReturnsNull()`
- `testInitializeWorkflow_MultipleWorkflows_SelectsCorrectOne()`
- `testInitializeWorkflow_ComplexQuestion_ParsesAllFields()`

### 4. WorkflowTypesTest
**Location:** `app/src/test/java/com/example/smartplay/workflow/WorkflowTypesTest.kt`

Tests data model classes:
- Creating Question and Workflow objects
- Serialization and deserialization with Gson
- Data class equality and copying
- Handling edge cases (empty lists, boundary values)
- Data class methods (copy, equals, hashCode)

**Key Test Cases:**
- `testQuestionCreation()`
- `testWorkflowSerializationAndDeserialization()`
- `testQuestionEquality()`
- `testQuestionFieldBoundaryValues()`

### 5. NotificationManagerTest
**Location:** `app/src/test/java/com/example/smartplay/workflow/notifications/NotificationManagerTest.kt`

Tests notification creation and delivery:
- Creating notifications for questions
- Setting correct notification content
- Adding action buttons for answers
- Handling various numbers of answer options
- Managing notification IDs and auto-cancel behavior

**Key Test Cases:**
- `testSendNotification_CreatesNotification()`
- `testSendNotification_HasCorrectContent()`
- `testSendNotification_HasCorrectNumberOfActions()`
- `testSendNotification_WithManyAnswers()`

### 6. RecordingActivityTest (Existing)
**Location:** `app/src/test/java/com/example/smartplay/RecordingActivityTest.kt`

Tests the main recording activity:
- Activity creation and initialization
- Starting, stopping, pausing, and resuming recording
- UI state updates
- Interaction with RecordingManager

### 7. SettingsActivityTest (Existing)
**Location:** `app/src/test/java/com/example/smartplay/SettingsActivityTest.kt`

Tests the settings activity functionality.

## Running Tests

### Using Android Studio

1. **Run All Tests:**
   - Open the Project view in Android Studio
   - Right-click on `app/src/test` folder
   - Select "Run 'Tests in 'com.example.smartplay''"

2. **Run Specific Test Class:**
   - Navigate to the test file you want to run
   - Right-click on the class name
   - Select "Run 'TestClassName'"

3. **Run Single Test Method:**
   - Open the test file
   - Click the green play icon next to the test method
   - Or right-click on the method and select "Run 'testMethodName'"

### Using Gradle Command Line

**Prerequisites:**
- Ensure the Gradle wrapper is properly set up
- If the wrapper is missing, regenerate it with: `gradle wrapper`

**Commands:**

```bash
# Run all unit tests
./gradlew test

# Run tests for debug build
./gradlew testDebugUnitTest

# Run tests for release build
./gradlew testReleaseUnitTest

# Run tests with detailed output
./gradlew test --info

# Run specific test class
./gradlew test --tests "com.example.smartplay.recording.FileUtilsTest"

# Run specific test method
./gradlew test --tests "com.example.smartplay.recording.FileUtilsTest.testReadFileFromAppSpecificDirectory_FileExistsInInternal"
```

### Using ADB (Android Debug Bridge)

For instrumentation tests (not applicable to current unit tests, but useful for future integration tests):

```bash
# Install the test APK
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk

# Run all tests
adb shell am instrument -w com.example.smartplay.test/androidx.test.runner.AndroidJUnitRunner
```

## Viewing Test Results

### Android Studio
- Results appear in the "Run" window at the bottom of the screen
- Green checkmarks indicate passed tests
- Red X marks indicate failed tests
- Click on any test to see detailed output

### Command Line
HTML reports are generated at:
```
app/build/reports/tests/testDebugUnitTest/index.html
```

Open this file in a browser to see:
- Overall test statistics (passed, failed, skipped)
- Test duration
- Detailed output for each test
- Stack traces for failures

## Test Dependencies

The project uses the following testing libraries (defined in `app/build.gradle`):

- **JUnit 4.13.2**: Core testing framework
- **Robolectric 4.9**: Android framework simulation for unit tests
- **AndroidX Test Core 1.5.0**: Android testing utilities
- **AndroidX Test JUnit 1.1.5**: JUnit extensions for Android
- **Mockito 3.12.4**: Mocking framework

## Writing New Tests

### Best Practices

1. **Test Naming:**
   - Use descriptive names: `test[MethodName]_[Scenario]_[ExpectedResult]()`
   - Example: `testWriteSensorData_WritesCorrectFormat()`

2. **Test Structure (AAA Pattern):**
   ```kotlin
   @Test
   fun testExample() {
       // Arrange - Set up test data and conditions
       val input = "test"

       // Act - Execute the method being tested
       val result = methodUnderTest(input)

       // Assert - Verify the results
       assertEquals(expected, result)
   }
   ```

3. **Setup and Teardown:**
   ```kotlin
   @Before
   fun setup() {
       // Initialize objects before each test
   }

   @After
   fun teardown() {
       // Clean up after each test
   }
   ```

4. **Using Robolectric:**
   ```kotlin
   @RunWith(RobolectricTestRunner::class)
   class MyTest {
       private lateinit var context: Context

       @Before
       fun setup() {
           context = RuntimeEnvironment.getApplication()
       }
   }
   ```

5. **Mocking with Mockito:**
   ```kotlin
   @Mock
   private lateinit var mockObject: MyClass

   @Before
   fun setup() {
       MockitoAnnotations.openMocks(this)
       `when`(mockObject.method()).thenReturn(value)
   }
   ```

### Example Test Template

```kotlin
package com.example.smartplay.mypackage

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.*

@RunWith(RobolectricTestRunner::class)
class MyNewTest {

    @Before
    fun setup() {
        // Initialize test objects
    }

    @Test
    fun testMethodName_Scenario_ExpectedResult() {
        // Arrange
        val input = "test input"

        // Act
        val result = methodUnderTest(input)

        // Assert
        assertEquals("expected output", result)
    }
}
```

## Troubleshooting

### Common Issues

1. **Gradle Wrapper Missing:**
   ```bash
   # Regenerate wrapper
   gradle wrapper
   ```

2. **Tests Not Found:**
   - Ensure test files are in `app/src/test/java/` directory
   - Check that test classes are annotated with `@RunWith(RobolectricTestRunner::class)`
   - Verify test methods are annotated with `@Test`

3. **Robolectric Shadow Errors:**
   - Update Robolectric version in `build.gradle`
   - Check Android SDK version compatibility
   - Ensure `testOptions { unitTests { includeAndroidResources = true } }` is set

4. **Context Not Available:**
   - Use `RuntimeEnvironment.getApplication()` for Robolectric tests
   - Ensure test is using `@RunWith(RobolectricTestRunner::class)`

5. **Mockito Errors:**
   - Initialize mocks with `MockitoAnnotations.openMocks(this)` in `@Before`
   - Use backticks for `when`: `` `when`(mock.method()) ``

## Continuous Integration

For CI/CD pipelines, add this to your workflow:

```yaml
# Example GitHub Actions workflow
- name: Run Unit Tests
  run: ./gradlew test

- name: Upload Test Reports
  uses: actions/upload-artifact@v2
  if: always()
  with:
    name: test-reports
    path: app/build/reports/tests/
```

## Test Coverage

To generate test coverage reports:

```bash
# Run tests with coverage
./gradlew testDebugUnitTest jacocoTestReport

# View coverage report
open app/build/reports/jacoco/jacocoTestReport/html/index.html
```

## Future Testing Improvements

- Add instrumentation tests for UI components
- Implement integration tests for end-to-end workflows
- Add performance tests for sensor data collection
- Create tests for background services
- Add UI tests with Espresso
- Implement code coverage requirements (target: 80%+)

## Contributing

When adding new features:
1. Write tests first (TDD approach recommended)
2. Ensure all existing tests pass
3. Add tests for edge cases and error scenarios
4. Update this documentation if adding new test categories

## Support

For testing questions or issues:
- Check Android Studio's "Build" and "Run" windows for error messages
- Review test output in HTML reports
- Consult Robolectric documentation: http://robolectric.org/
- Check JUnit 4 documentation: https://junit.org/junit4/
