# SmartPlay
An Android OS Wear Application to measure children's activity while playing.

## Setting Workflows
To set up workflows, place the `workflows.json` file in the following path via the Device File Explorer:
```shell
/storage/emulated/0/Android/data/com.example.smartplay/files/workflows.json
```
You can find a demo `workflows.json` file in the `app/sampledata` folder of this repository.

## Initial Setup (New Watch)
For a Samsung Galaxy Watch v6 and above:

1. **Pair the Watch**:
   - Pair the watch with an Android phone using the Samsung Wearable app.
2. **Enable Developer Mode on the Watch**:
   - Go to `Settings` -> `About the watch` -> `Software information`.
   - Tap the "Software information" tile multiple times until 'Developer mode turned on' message appears.
3. **Connect the Watch to a Computer**:
   - Connect the watch to your computer via cable or Wi-Fi. (See the steps below for Wi-Fi Debugging)

## ⌚️ Installing the Application on the Device
Ensure `adb` is installed on your computer and the watch is connected via USB or Wi-Fi.

```shell
adb -s <device_id> install -r /path/to/your/app-debug.apk
```

Note: Installation via Wi-Fi Debugging is recommended. Please refer to the Wi-Fi Debugging section below.

## Recording Files
After a recording session, files will be generated and saved in the Documents directory. The filename format is `[user_id]_[type]_[device_id]_[timestamp]`.

### Filename Breakdown:
- **user_id**: Numeric identifier set on the settings screen.
- **type**: Type of data in the file:
  - `SENSORS`: Sensors data.
  - `AUDIO`: Audio recordings.
  - `BT`: Bluetooth scan data.
  - `QUESTIONS`: Responses to workflow questions.
- **device_id**: Unique identifier for the device (e.g., `faaab8a5585c9531`).
- **timestamp**: Time when the recording was made, ensuring a unique identifier.

Example: `1_AUDIO_faaab8a5585c9531_1717009923893.3gp` represents an audio recording for user 1, captured by device `faaab8a5585c9531` at the timestamp `1717009923893`.

## Privacy
Files are stored in the internal storage of the app's private directory. They are not directly accessible via the SD card or file explorer apps unless the device is rooted.

## 🛜 Connecting the Smartwatch via Wi-Fi Debugging
If installation via USB doesn't work, follow these steps for Wi-Fi Debugging:

1. On the watch, go to `Settings -> Developer options`.
2. Enable `Developer options`.
3. Enable `ADB debugging`.
4. Go to `Wireless debugging` and click `+ Pair new device`.
   - Note the IP address & port displayed on the watch.
5. On your computer, run:
   ```shell
   adb pair <YOUR_IP_ADDRESS:PORT>
   ```
6. Once paired, connect by running:
   ```shell
   adb connect <YOUR_IP_ADDRESS:PORT>
   ```

### Install APK via Wi-Fi
Once connected, download the SmartPlay application(.apk) and install it on the whatch usin gyour computer terminal:
```shell
adb -s ip:port install </path/to/your/app-debug.apk>
```

If having issues, restart the adb server:
```shell
adb kill-server
adb start-server
```

## 🛠️ Building the App with Android Studio
You can build the app using Android Studio by following the standard build process.

![Build Process](https://github.com/ctwhome/SmartPlay/assets/4195550/ff8c7315-226e-464b-80a8-f83cd2692d71)

## Useful Commands
| Description             | Command                                                   |
| ----------------------- | --------------------------------------------------------- |
| Connect to a device     | `adb connect 192.168.1.64:5555`                           |
| Install app             | `adb -s 192.168.1.148:5555 install ./app-debug.apk`       |
| Uninstall app           | `adb -s 192.168.1.148:5555 uninstall com.example.smartplay`|
| List installed packages | `adb -s 192.168.1.148:5555 shell pm list packages`        |
