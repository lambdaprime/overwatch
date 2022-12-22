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

import static id.overwatch.Util.localizeObject;
import static id.overwatch.Util.sleep;

import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.function.BiConsumer;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class DeltaExtractor implements Subscriber<Mat> {

    private Subscription subscription;
    private BiConsumer<Boolean, Mat> consumer;
    private Mat firstFrame;

    public DeltaExtractor(BiConsumer<Boolean, Mat> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onComplete() {}

    @Override
    public void onError(Throwable throwable) {}

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    void print(Mat m) {
        System.out.println("-----");
        System.out.println(m.dump());
    }

    public void onNext(Mat frame) {
        if (firstFrame == null) firstFrame = frame;

        var frameDelta = new Mat();
        Core.absdiff(firstFrame, frame, frameDelta);
        // Imgproc.erode(frameDelta, frameDelta, new Mat());

        var lo = new Scalar(15, 15, 15);
        var hi = new Scalar(255, 255, 255);

        var mask = new Mat();
        Core.inRange(frameDelta, lo, hi, mask);

        var maskRgb = thresholdRgb(frameDelta, lo, hi);
        Optional<Rect> object = localizeObject(mask);

        var newFrame = new Mat();
        Core.bitwise_and(frame, maskRgb, newFrame);
        Imgproc.cvtColor(maskRgb, mask, Imgproc.COLOR_BGR2GRAY);
        //        Imgproc.dilate(mask, mask, new Mat());
        Imgproc.threshold(mask, mask, 0, 255, Imgproc.THRESH_BINARY);
        newFrame = addAlpha(newFrame, mask);

        if (object.isPresent()) {
            newFrame = new Mat(newFrame, object.get());
        }

        consumer.accept(object.isPresent(), newFrame);
        sleep(600);
        subscription.request(1);
    }

    private Mat addAlpha(Mat mat, Mat alpha) {
        var mv = new LinkedList<Mat>();
        Core.split(mat, mv);
        mv.addLast(alpha);
        var ret = new Mat();
        Core.merge(mv, ret);
        return ret;
    }

    /**
     * OpenCV inRange for doing threshold in RGB images will return 255 only when lo <= pixel[x][y]
     * <= hi across all channels. This method returns 255 if lo <= pixel[x][y] <= hi in at least one
     * of channels.
     *
     * <p>The other difference is the number of channels in the output array. For inRange it always
     * 1 and this method will return 3.
     */
    private Mat thresholdRgb(Mat m, Scalar lo, Scalar hi) {
        if (m.type() != CvType.CV_8UC3) throw new RuntimeException("Non 8UC3 frame delta");
        Mat nm = m.clone();
        for (int i = 0; i < m.cols(); i++) {
            for (int j = 0; j < m.rows(); j++) {
                byte[] buf = new byte[3];
                m.get(j, i, buf);
                int v1 = Byte.toUnsignedInt(buf[0]);
                int v2 = Byte.toUnsignedInt(buf[1]);
                int v3 = Byte.toUnsignedInt(buf[2]);
                boolean isOk = false;
                isOk |= lo.val[0] <= v1 && v1 <= hi.val[0];
                isOk |= lo.val[1] <= v2 && v2 <= hi.val[1];
                isOk |= lo.val[2] <= v3 && v3 <= hi.val[2];
                // System.out.format("%d, %d, %d - %s\n", r, g, b, isOk);
                buf[0] = (byte) (isOk ? -1 : 0);
                buf[1] = (byte) (isOk ? -1 : 0);
                buf[2] = (byte) (isOk ? -1 : 0);
                nm.put(j, i, buf);
            }
        }
        return nm;
    }
}
