package com.universal.thread;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolWorker {

    private int minPoolSize = 5;
    private int maxPoolSize = Integer.MAX_VALUE;
    private ThreadPoolExecutor threadPoolExecutor = null;

    public ThreadPoolWorker() {

        threadPoolExecutor = new ThreadPoolExecutor(minPoolSize, maxPoolSize, 30L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    }

    public void execute(Runnable worker) {
        threadPoolExecutor.execute(worker);
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public void setMinPoolSize(int minPoolSize) {
        this.minPoolSize = minPoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }
}
