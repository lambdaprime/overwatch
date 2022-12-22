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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public class CameraSubscription<T> implements Subscription {

    private ExecutorService executor;
    private Subscriber<T> subscriber;
    private Supplier<T> frames;
    private Future<?> future;

    public CameraSubscription(
            Subscriber<T> subscriber, ExecutorService executor, Supplier<T> frames) {
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
        future =
                executor.submit(
                        () -> {
                            subscriber.onNext(frames.get());
                        });
    }
}
