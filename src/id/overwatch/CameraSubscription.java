package id.overwatch;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public class CameraSubscription <T> implements Subscription {

    private ExecutorService executor;
    private Subscriber<T> subscriber;
    private Supplier<T> frames;
    private Future<?> future;

    public CameraSubscription(Subscriber<T> subscriber, ExecutorService executor, Supplier<T> frames) {
        this.subscriber = subscriber;
        this.executor = executor;
        this.frames = frames;
    }

    @Override
    public void cancel() {
        future.cancel(false);
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        subscriber.onComplete();
    }

    @Override
    public void request(long n) {
        if (n != 1) {
            throw new IllegalArgumentException();
        }
        future = executor.submit(() -> {
            subscriber.onNext(frames.get());
        });
    }

}
