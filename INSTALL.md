# Installation and Setup Instructions for SmartPlay

## 1. Preparing the Device
![Slide 16_9 - 7](https://github.com/user-attachments/assets/0687cc2d-b639-446b-88f6-b5981bda369b)

### 1.1 Enable Developer Mode on the Watch
1. On your watch, go to **Settings** → **Software Information**.
2. Tap **Software Version** multiple times until Developer Mode is enabled.

### 1.2 Enable Wifi Debugging
1. Enable **Developer Options**, **ADB Debugging**, and **Debug over WiFi** on your watch.
2. Once enabled, note the IP address and port displayed (e.g., `192.168.1.64:5555`).

### 1.3 Connect via ADB
To connect via ADB, run:
```bash
adb connect <ip:port>
```
Replace `<ip:port>` with the actual IP and port.

### 1.4 Restart ADB Server (if needed)
If you encounter issues during connection:
```bash
adb kill-server
adb start-server
```

## 2. Installing the SmartPlay Application

### 2.1 Install via ADB
1. **Single Device Installation**:
    ```bash
    adb install path/to/your/smartplay.apk
    ```
2. **Multiple Devices**: Specify the device ID:
    ```bash
    adb -s <device_id:port> install path/to/your/smartplay.apk
    ```
Replace `path/to/your/smartplay.apk` with the actual path to the SmartPlay APK file.

## 3. Adding the Workflows File

### 3.1 Set Up Workflows Directory
After installing the app, manually create the necessary directory on the device:
```bash
adb -s <device_id:port> shell mkdir -p /sdcard/Android/data/com.example.smartplay/files/
```

### 3.2 Push workflows.json to Device
1. **Copy the workflows.json** file to the app’s data directory:
    ```bash
    adb -s <device_id:port> push path/to/your/workflows.json /sdcard/Android/data/com.example.smartplay/files/workflows.json
    ```
2. **Set Permissions**:
    ```bash
    adb shell chmod 644 /sdcard/Android/data/com.example.smartplay/files/workflows.json
    ```
Ensure the file is added before launching the app for the first time.

## 4. Launching the Application

### 4.1 Launch via ADB
- **Single Device**:
    ```bash
    adb shell am start -n com.example.smartplay/.MainActivity
    ```
- **Multiple Devices**:
    ```bash
    adb -s <device_id:port> shell am start -n com.example.smartplay/.MainActivity
    ```

## 5. Managing the SmartPlay Application

### 5.1 Uninstall the App
```bash
adb uninstall com.example.smartplay
```

### 5.2 Force-Stop the App
```bash
adb shell am force-stop com.example.smartplay
```

### 5.3 Clear App Data
```bash
adb shell pm clear com.example.smartplay
```

## 6. Recording Files

### 6.1 File Storage
- Files generated during a recording session are saved in the **Documents** directory with the format `[user_id]_[type]_[device_id]_[timestamp]`.

### 6.2 File Naming
- **user_id**: The number identifier of the user set in the settings.
- **type**: Type of data (e.g., `SENSORS`, `AUDIO`, `BT`, `QUESTIONS`).
- **device_id**: Unique identifier for the device.
- **timestamp**: Exact recording time.

Example: `1_AUDIO_faaab8a5585c9531_1717009923893.3gp`

## 7. Retrieving Files from the Device

### 7.1 Pull Files
To retrieve files from the device:
```bash
adb -s <device_id:port> pull /sdcard/Android/data/com.example.smartplay/files/Documents ./
```
This will copy the contents from the **Documents** directory to the current directory on your computer.

## 8. Connecting the Smartwatch via WiFi Debugging
If installing over USB fails, try **WiFi Debugging**:
1. **Enable Developer Options, ADB Debugging, and Debug Over WiFi** on the watch.
2. Connect using:
    ```bash
    adb connect ip:port
    ```
3. Install APK:
    ```bash
    adb -s ip:port install path/to/your/app-debug.apk
    ```

## 9. Additional Notes
- Replace `<device_id:port>` with the actual device ID and port.
- If you face permission issues while pushing the `workflows.json`, ensure:
  - The app has been installed and launched at least once.
  - The required directories are created.

## 10. Useful ADB Commands

| Description             | Command                                                   |
| ----------------------- | --------------------------------------------------------- |
| Connect to a device     | `adb connect 192.168.1.64:5555`                           |
| Install app             | `adb -s 192.168.1.148:5555 install ./app-debug.apk`       |
| Uninstall app           | `adb -s 192.168.1.148:5555 uninstall com.example.smartplay`|
| List installed packages | `adb -s 192.168.1.148:5555 shell pm list packages`        |
