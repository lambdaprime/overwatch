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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

public class Camera implements Publisher<Mat> {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private VideoCapture capture;
    private Size size;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public Camera(int cameraId, int h, int w) {
        capture = new VideoCapture(cameraId);
        if (!capture.isOpened()) {
            throw new RuntimeException("Cannot open the camera");
        }
        size = new Size(w, h);
        prewarm();
    }

    private void prewarm() {
        System.out.print("Prewarming camera...");
        Instant delay = Instant.now().plus(7, ChronoUnit.SECONDS);
        var frame = new Mat();
        while (Instant.now().isBefore(delay)) capture.read(frame);
        System.out.println("OK");
    }

    @Override
    public void subscribe(Subscriber<? super Mat> subscriber) {
        subscriber.onSubscribe(
                new CameraSubscription<Mat>(
                        (Subscriber<Mat>) subscriber, executor, this::readFrame));
    }

    private Mat readFrame() {
        var frame = new Mat();
        capture.read(frame);
        frame = resize(frame, size);
        return frame;
    }

    /**
     * Resize preserving ratio
     *
     * @param size final size of the longest side
     * @return
     */
    Mat resize(Mat img, Size size) {
        Size s;
        double r = img.size().width / img.size().height;
        if (img.size().height > img.size().width) s = new Size(size.width * r, size.height);
        else s = new Size(size.width, size.height / r);
        var tmp = new Mat(s, img.type());
        Imgproc.resize(img, tmp, tmp.size());
        Mat tmp2 = Mat.zeros((int) size.height, (int) size.width, img.type());
        tmp.copyTo(
                tmp2.submat(
                        new Rect(
                                new double[] {
                                    (size.width - s.width) / 2,
                                    (size.height - s.height) / 2,
                                    s.width,
                                    s.height
                                })));
        return tmp2;
    }
}
