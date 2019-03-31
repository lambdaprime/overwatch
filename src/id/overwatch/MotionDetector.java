package id.overwatch;

import static id.overwatch.Util.localizeObject;
import static id.overwatch.Util.sleep;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

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
    public void onComplete() {

    }

    @Override
    public void onError(Throwable throwable) {

    }

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

        if (prevFrame == null)
            prevFrame = tmp;

        var frameDelta = new Mat();
        Core.absdiff(prevFrame, tmp, frameDelta);
        prevFrame = tmp;

        Imgproc.threshold(frameDelta, frameDelta, 25, 255, Imgproc.THRESH_BINARY);
        Imgproc.dilate(frameDelta, frameDelta, new Mat());
        Optional<Rect> object = localizeObject(frameDelta);
        object.ifPresent(roi -> {
            Imgproc.rectangle(frame,
                    roi.tl(),
                    roi.br(),
                    new Scalar(0, 255, 0), 2);
        });

        consumer.accept(object.isPresent(), frame);
        sleep(300);
        subscription.request(1);
    }

}
