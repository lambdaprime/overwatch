package id.overwatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
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
        var contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(frameDelta, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        List<Point> points = contours.stream()
                .filter(c -> Imgproc.contourArea(c) > 10)
                .map(c -> Imgproc.boundingRect(c))
                .flatMap(rect -> Stream.of(new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height)))
                .collect(Collectors.toList());

        if (!points.isEmpty()) {
            double minX = points.stream().map(p -> p.x).min(Double::compareTo).get();
            double maxX = points.stream().map(p -> p.x).max(Double::compareTo).get();
            double minY = points.stream().map(p -> p.y).min(Double::compareTo).get();
            double maxY = points.stream().map(p -> p.y).max(Double::compareTo).get();
            Imgproc.rectangle(frame,
                    new Point(minX, maxY),
                    new Point(maxX, minY),
                    new Scalar(0, 255, 0), 2);
        }

        consumer.accept(!points.isEmpty(), frame);
        sleep(300);
        subscription.request(1);
    }

    private int mostLeft(Point p1, Point p2) {
        if (p1.x == p2.x) return (int) (p1.y - p2.y);
        return (int) (p1.x - p2.x);
    }

    private void sleep(int msec) {
        try {
            TimeUnit.MILLISECONDS.sleep(msec);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
