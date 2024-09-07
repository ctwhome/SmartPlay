# SmartPlay
Android OS Wear Application to measure children's activity when playing

## Setting Workflows
Inside the Device File Explorer, set the workflows file in this path:
```shell
/storage/emulated/0/Android/data/com.example.smartplay/files/workflows.json
```
You can find the workflows.json demo file in the app/sampledata folder of this repository.

## Installing and Setting Up SmartPlay

### Installing the application via ADB
1. Make sure you have ADB (Android Debug Bridge) installed on your computer.
2. Connect your smartwatch to your computer via USB or WiFi debugging.
3. Open a terminal or command prompt and run the following command:
   ```
   adb install -r path/to/your/smartplay.apk
   ```
   Replace `path/to/your/smartplay.apk` with the actual path to the SmartPlay APK file.

### Adding workflows.json before launching the app
1. After installing the app, copy the workflows.json file to the app's data directory:
   ```
   adb push path/to/your/workflows.json /sdcard/Android/data/com.example.smartplay/files/workflows.json
   ```
   Replace `path/to/your/workflows.json` with the actual path to your workflows.json file.

2. Set the proper permissions for the file:
   ```
   adb shell chmod 644 /sdcard/Android/data/com.example.smartplay/files/workflows.json
   ```

3. Ensure the workflows.json file is in place before launching the app for the first time.

### ⌚️ Installing the application on the device
Make sure you have adb installed on your computer and the watch is connected to your computer, via usb or wifi.
Note: I could only install the application via Wifi Debugging, check the notes below on how to install it.

```shell
adb -s <device_id> install -r /path/to/your/app-debug.apk
```

Path to getting the data from the device:
![Screenshot 2023-07-24 at 21 50 14](https://github.com/ctwhome/SmartPlay/assets/4195550/cfc87b19-d0e8-41cc-ba41-6c2abad2a9c8)

## Recording files
When a recording session is completed, several files are generated and saved in the Documents directory. The files are named using the format [user_id]_[type]_[device_id]_[timestamp].

Here's a brief description of each part of the filename:

- user_id: The number identifier of the user set in the setting screen.
- type: The type of data contained in the file. This can be:
  - SENSORS for the array of sensors collected.
  - AUDIO for audio recordings.
  - BT for Bluetooth scan data.
  - QUESTIONS for responses to workflow questions.
- device_id: A unique identifier for the device, such as faaab8a5585c9531 in the example.
- timestamp: The exact time when the recording was made, ensuring each file has a unique identifier.

For example, a filename like 1_AUDIO_faaab8a5585c9531_1717009923893.3gp indicates an audio recording for user 1, captured by device faaab8a5585c9531 at the timestamp 1717009923893.

## Privacy
The generated files are stored in the internal storage of the app's private directory, which is not directly accessible via the SD card or other file explorer apps unless your device is rooted.

## 🛜 Connecting the smartwatch via Wifi Debugging
I couldn't make the installation of the application work via usb cable, but yes over wifi debugging.

On the watch
1. Enable developer options
2. Enable ADB debugging
3. Enable Debug over wifi (it will take a moment to establish an ip:port)

Once you have the ip:port, you can connect to the watch via adb
```shell
adb connect ip:port
```
Now you can install the APK: Now, you can install the APK with the adb install command. Replace /path/to/your/app-debug.apk with the path to your APK file:
```
adb -s ip:port install /path/to/your/app-debug.apk
```


During the installation, I had to restart the adb server to make it work at times.
```shell
adb kill-server
adb start-server
```


## 🛠️ Building the App with Android Studio
![image](https://github.com/ctwhome/SmartPlay/assets/4195550/ff8c7315-226e-464b-80a8-f83cd2692d71)

## Useful commands
| Description             | Command                                                   |
| ----------------------- | --------------------------------------------------------- |
| Connect to a device     | adb connect 192.168.1.64:5555                             |
| Install app             | adb -s 192.168.1.148:5555 install ./app-debug.apk         |
| Uninstall app           | adb -s 192.168.1.148:5555 uninstall com.example.smartplay |
| List installed packages | adb -s 192.168.1.148:5555 shell pm list packages          |
