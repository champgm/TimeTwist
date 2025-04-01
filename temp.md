adb connect 192.168.2.224:36297 

adb -s 192.168.2.224:36297 uninstall com.cgm.timetwist 

adb -s 192.168.2.224:36297 install -r .\app\release\app-release.apk 
