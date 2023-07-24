# SmartPlay
Android OS Wear Application to measure children's activity when playing


### Installing the application on the device
Make sure you have adb installed on your computer and 
1. Connect the watch to your computer
```shell
adb -s <device_id> install -r /path/to/your/app-debug.apk
```


Notes: 
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