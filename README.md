# SmartPlay
Android OS Wear Application to measure children's activity when playing


### Installing the application on the device
Make sure you have adb installed on your computer and 
1. Connect the watch to your computer
```shell
adb -s <device_id> install -r /path/to/your/app-debug.apk
```

Getting the data from the device:
![Screenshot 2023-07-24 at 21 50 14](https://github.com/ctwhome/SmartPlay/assets/4195550/cfc87b19-d0e8-41cc-ba41-6c2abad2a9c8)

Building the App with Android Studio
<img width="430" alt="image" src="https://github.com/ctwhome/SmartPlay/assets/4195550/4fce25a8-f6ba-426d-b83f-530c25117c81">



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
