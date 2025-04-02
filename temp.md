adb connect 192.168.2.224:41321 

adb -s 192.168.2.224:41321 uninstall com.cgm.timetwist 

adb -s 192.168.2.224:41321 install -r .\app\release\app-release.apk 


adb connect adb-27101JEEJW0064-gneYdC._adb-tls-connect._tcp
adb -s adb-27101JEEJW0064-gneYdC._adb-tls-connect._tcp uninstall com.cgm.timetwist
adb -s adb-27101JEEJW0064-gneYdC._adb-tls-connect._tcp install -r .\app\release\app-release.apk 