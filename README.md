**overwatch** - program which receives video stream from the camera and capture images if it detects any movements. The images are saved for later processing.

lambdaprime <id.blackmesa@gmail.com>

# Download

You can download **overwatch** from [here](https://github.com/lambdaprime/overwatch/blob/master/release/overwatch.zip)

# Requirements

Java 11, libopencv3.2-java

# Usage

```
java -jar overwatch.jar <OUTPUT_DIR>
```

Where: 

* OUTPUT_DIR - directory where images are going to be stored.

# Examples

```
java -jar overwatch.jar /tmp/day1
```
