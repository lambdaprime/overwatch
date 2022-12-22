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

import java.util.Optional;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.function.BiConsumer;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MotionDetector implements Subscriber<Mat> {

    private Subscription subscription;
    private BiConsumer<Boolean, Mat> consumer;
    private Mat prevFrame;

    public MotionDetector(BiConsumer<Boolean, Mat> consumer) {
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

    @Override
    public void onNext(Mat frame) {
        var tmp = new Mat();
        Imgproc.cvtColor(frame, tmp, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(tmp, tmp, new Size(21, 21), 0);

        if (prevFrame == null) prevFrame = tmp;

        var frameDelta = new Mat();
        Core.absdiff(prevFrame, tmp, frameDelta);
        prevFrame = tmp;

        Imgproc.threshold(frameDelta, frameDelta, 25, 255, Imgproc.THRESH_BINARY);
        Imgproc.dilate(frameDelta, frameDelta, new Mat());
        Optional<Rect> object = localizeObject(frameDelta);
        object.ifPresent(
                roi -> {
                    Imgproc.rectangle(frame, roi.tl(), roi.br(), new Scalar(0, 255, 0), 2);
                });

        consumer.accept(object.isPresent(), frame);
        sleep(300);
        subscription.request(1);
    }
}
