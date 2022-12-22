/*
 * Copyright 2022 overwatch project
 * 
 * Website: https://github.com/lambdaprime/overwatch
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package id.overwatch;

import id.util.SmartArgs;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;

public class Overwatch {

    private static String OUTPUT_DIR;
    private static boolean DELTA;
    private static Optional<Integer> cameraId = Optional.empty();
    private static final Function<String, Boolean> defaultHandler =
            arg -> {
                switch (arg) {
                    case "-d":
                        DELTA = true;
                        break;
                    default:
                        OUTPUT_DIR = arg;
                }
                return true;
            };
    private static final Map<String, Consumer<String>> handlers =
            Map.of("-c", id -> cameraId = Optional.of(Integer.parseInt(id)));

    private static int HEIGHT = 300;
    private static int WIDTH = 300;

    @SuppressWarnings("resource")
    static void usage() throws IOException {
        Scanner scanner =
                new Scanner(Overwatch.class.getResource("/README-overwatch.md").openStream())
                        .useDelimiter("\n");
        while (scanner.hasNext()) System.out.println(scanner.next());
    }

    public static void main(String[] args) throws Exception {
        try {
            new SmartArgs(handlers, defaultHandler).parse(args);
            if (OUTPUT_DIR == null) throw new RuntimeException();
        } catch (Exception e) {
            usage();
            System.exit(1);
        }
        Files.createDirectories(Paths.get(OUTPUT_DIR));
        var camera = new Camera(cameraId.orElse(0), HEIGHT, WIDTH);
        var canvas = new MyCanvas(HEIGHT, WIDTH);
        if (DELTA) {
            camera.subscribe(
                    new DeltaExtractor(
                            (isChanged, m) -> {
                                if (isChanged) {
                                    Path p =
                                            Paths.get(
                                                    OUTPUT_DIR,
                                                    "overwatch"
                                                            + System.currentTimeMillis()
                                                            + ".png");
                                    Imgcodecs.imwrite(
                                            p.toString(),
                                            m,
                                            new MatOfInt(Imgcodecs.IMWRITE_PNG_COMPRESSION));
                                }
                                canvas.update(m);
                            }));
        } else {
            camera.subscribe(
                    new MotionDetector(
                            (isChanged, m) -> {
                                if (isChanged) {
                                    Path p =
                                            Paths.get(
                                                    OUTPUT_DIR,
                                                    "overwatch"
                                                            + System.currentTimeMillis()
                                                            + ".jpg");
                                    Imgcodecs.imwrite(p.toString(), m);
                                }
                                canvas.update(m);
                            }));
        }
    }
}
