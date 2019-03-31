package id.overwatch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Scanner;
import java.util.function.Function;

import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;

import id.util.SmartArgs;

public class Overwatch {

    private static String OUTPUT_DIR;
    private static boolean DELTA;
    private static final Function<String, Boolean> defaultHandler = arg -> {
        switch (arg) {
        case "-d": DELTA = true; break;
        default: OUTPUT_DIR = arg;
        }
        return true;
    };

    private static int HEIGHT = 300;
    private static int WIDTH = 300;

    @SuppressWarnings("resource")
    static void usage() throws IOException {
        Scanner scanner = new Scanner(Overwatch.class.getResource("README.md").openStream())
                .useDelimiter("\n");
        while (scanner.hasNext())
            System.out.println(scanner.next());
    }

    public static void main(String[] args) throws Exception {
        try {
            new SmartArgs(Collections.emptyMap(), defaultHandler).parse(args);
            if (OUTPUT_DIR == null) throw new RuntimeException();
        } catch (Exception e) {
            usage();
            System.exit(1);
        }
        Files.createDirectories(Paths.get(OUTPUT_DIR));
        var camera = new Camera(HEIGHT, WIDTH);
        var canvas = new MyCanvas(HEIGHT, WIDTH);
        if (DELTA) {
            camera.subscribe(new DeltaExtractor((isChanged, m) -> {
                if (isChanged) {
                    Path p = Paths.get(OUTPUT_DIR, "overwatch" + System.currentTimeMillis() + ".png");
                    Imgcodecs.imwrite(p.toString(), m, new MatOfInt(Imgcodecs.CV_IMWRITE_PNG_COMPRESSION));
                }
                canvas.update(m);

            }));
        } else {
            camera.subscribe(new MotionDetector((isChanged, m) -> {
                if (isChanged) {
                    Path p = Paths.get(OUTPUT_DIR, "overwatch" + System.currentTimeMillis() + ".jpg");
                    Imgcodecs.imwrite(p.toString(), m);
                }
                canvas.update(m);
            }));
        }
    }

}
