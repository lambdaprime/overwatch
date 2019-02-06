package id.overwatch;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.opencv.imgcodecs.Imgcodecs;

public class Overwatch {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.format("Usage: %s <OUTPUT_DIR>\n", Overwatch.class.getName());
            return;
        }
        Files.createDirectories(Paths.get(args[0]));
        var camera = new Camera(300, 300);
        var canvas = new MyCanvas(300, 300);
        camera.subscribe(new MotionDetector((isChanged, m) -> {
            if (isChanged) {
                Path p = Paths.get(args[0], "overwatch" + System.currentTimeMillis() + ".jpg");
                Imgcodecs.imwrite(p.toString(), m);
            }
            canvas.update(m);
        }));
    }

}
