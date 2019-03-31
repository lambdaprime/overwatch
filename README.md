**overwatch** - program which receives video stream from the camera and capture images if it detects any movements. The images are saved for later processing.

lambdaprime <id.blackmesa@gmail.com>

# Download

You can download **overwatch** from [here](https://github.com/lambdaprime/overwatch/blob/master/release/overwatch.v2.zip)

# Requirements

Java 11, libopencv3.2-java

# Usage

```
java -jar overwatch.jar [-d] <OUTPUT_DIR>
```

Where: 

* OUTPUT_DIR -- directory where images are going to be stored.
* -d -- will make **overwatch** to save only delta between the first frame and the new one. 

# Examples

```
java -jar overwatch.jar /tmp/day1
```
