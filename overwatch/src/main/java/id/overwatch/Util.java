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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

public class Util {

    public static void sleep(int msec) {
        try {
            TimeUnit.MILLISECONDS.sleep(msec);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static Optional<Rect> localizeObject(Mat frame) {
        var contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(
                frame, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        List<Point> points =
                contours.stream()
                        .filter(c -> Imgproc.contourArea(c) > 10)
                        .map(c -> Imgproc.boundingRect(c))
                        .flatMap(
                                rect ->
                                        Stream.of(
                                                new Point(rect.x, rect.y),
                                                new Point(
                                                        rect.x + rect.width, rect.y + rect.height)))
                        .collect(Collectors.toList());

        if (!points.isEmpty()) {
            double minX = points.stream().map(p -> p.x).min(Double::compareTo).get();
            double maxX = points.stream().map(p -> p.x).max(Double::compareTo).get();
            double minY = points.stream().map(p -> p.y).min(Double::compareTo).get();
            double maxY = points.stream().map(p -> p.y).max(Double::compareTo).get();
            return Optional.of(new Rect(new Point(minX, maxY), new Point(maxX, minY)));
        }

        return Optional.empty();
    }
}
