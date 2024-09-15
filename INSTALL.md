Here’s an improved and clearer version of your installation instructions based on the new structure:

---

# Installation and Setup Instructions for SmartPlay

## 1. Enable Developer Mode on the Watch
1. On your watch, navigate to **Settings** → **Software Information**.
2. Tap on **Software Version** multiple times until you see a message indicating that Developer Mode is enabled.

## 2. Install the App
- **For a single device** connected to your computer:

    ```bash
    adb install app-release.apk
    ```

- **For multiple devices** connected, specify the device using `-s` with the device ID:

    ```bash
    adb -s <device_id:port> install app-release.apk
    ```

## 3. Add the Workflows File

To add the `workflows.json` file to the app’s data directory, follow these steps:

### Manually Create the Directory and Push the File
1. **Create the necessary directory** on the device:
    ```bash
    adb -s <device_id:port> shell mkdir -p /sdcard/Android/data/com.example.smartplay/files/Documents
    ```

2. **Push the workflows.json file** to the app’s directory:
    ```bash
    adb -s <device_id:port> push ../sampledata/workflows.json /sdcard/Android/data/com.example.smartplay/files/
    ```

# Retrieve Files from the Device
To pull files from the device to your computer:

```bash
adb -s <device_id:port> pull /sdcard/Android/data/com.example.smartplay/files/Documents ./
```

This will retrieve all files from the `/Documents` directory in the app's data folder and copy them to the current directory on your computer.


## 6. Other Useful Commands

### Launch the App
- **To launch the app** on the connected device:

    ```bash
    adb shell am start -n com.example.smartplay/.MainActivity
    ```

- **For multiple devices**, specify the device ID:

    ```bash
    adb -s <device_id:port> shell am start -n com.example.smartplay/.MainActivity
    ```

### Uninstall the App
If you need to uninstall the app:

```bash
adb uninstall com.example.smartplay
```

### Force-Stop the App
To force-stop the app (e.g., if it’s not responding):

```bash
adb shell am force-stop com.example.smartplay
```

### Clear App Data
To reset the app by clearing its data:

```bash
adb shell pm clear com.example.smartplay
```

---

## Additional Notes:
- Replace `<device_id:port>` with the actual device ID and port if multiple devices are connected.
- If the `workflows.json` file push or retrieval fails due to permission issues, ensure that:
  - The app is installed and has been run at least once.
  - The necessary directories are created by running the app.

By following these steps, you should be able to install the app, add the `workflows.json` file, launch the app, and retrieve any files from the device.
