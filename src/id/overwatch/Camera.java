package id.overwatch;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;

import org.opencv.core.Core;
import org.opencv.core.Mat;
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

    public Camera(int h, int w) {
        capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            throw new RuntimeException("Cannot open the camera");
        }
        size = new Size(w, h);
    }

    @Override
    public void subscribe(Subscriber<? super Mat> subscriber) {
        subscriber.onSubscribe(new CameraSubscription<Mat>((Subscriber<Mat>) subscriber,
                executor, this::readFrame));		
    }

    private Mat readFrame() {
        var frame = new Mat();
        capture.read(frame);
        Imgproc.resize(frame, frame, size);
        return frame;
    }
}
