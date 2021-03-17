package br.com.alura.estoque.asynctask;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskRunner {

    private final Executor executor = Executors.newCachedThreadPool();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public <R> void executeAsync(AsyncTask<R> callable, Callback<R> callback) {
        executor.execute(() -> {
            final R result = callable.call();
            handler.post(() -> {
                callback.onComplete(result);
            });
        });
    }

    public interface Callback<R> {
        void onComplete(R result);
    }

}
