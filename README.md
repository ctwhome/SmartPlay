# SmartPlay
Android OS Wear Application to measure children's activity when playing

## Setting Workflows
Inside the Device File Explorer, set the workflows file in this path:
```shell
/storage/emulated/0/Android/data/com.example.smartplay/files/workflows.json
```
You can find the workflows.json demo file in the app/sampledata folder of this repository.

### ⌚️ Installing the application on the device
Make sure you have adb installed on your computer and the watch is connected to your computer, via usb or wifi.
Note: I could only install the application via Wifi Debugging, check the notes below on how to install it. 

```shell
adb -s <device_id> install -r /path/to/your/app-debug.apk
```

Path to getting the data from the device:
![Screenshot 2023-07-24 at 21 50 14](https://github.com/ctwhome/SmartPlay/assets/4195550/cfc87b19-d0e8-41cc-ba41-6c2abad2a9c8)


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
