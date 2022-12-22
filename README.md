**overwatch** - program which receives video stream from the camera and capture images if it detects any movements. The images are saved for later processing.

# Download

You can download **overwatch** from [here](https://github.com/lambdaprime/overwatch/blob/master/release/)

# Requirements

Java 17, libopencv-java >= 4.2

# Usage

```
java -Djava.library.path=LIB_PATH -p MODULE_PATH --add-modules opencv -jar overwatch.jar [-d] [-c CAMERA_ID] <OUTPUT_DIR>
```

Where: 

* LIB_PATH -- directory with OpenCV native library (ex. libopencv_java320.so)
* MODULE_PATH -- directory with OpenCV Java library (ex. opencv.jar)
* OUTPUT_DIR -- directory where images are going to be stored.
* -d -- will make **overwatch** to save only delta between the first frame and the new one. 
* -c -- specify which camera to use. By default takes camera with 0 identifier.

# Examples

```
java  -Djava.library.path=/usr/lib/jni -p /usr/share/java/opencv.jar --add-modules opencv -jar overwatch.jar /tmp/day1
```

# Contributors

lambdaprime <intid@protonmail.com>
