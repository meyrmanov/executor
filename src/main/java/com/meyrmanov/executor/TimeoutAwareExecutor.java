package com.meyrmanov.executor;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeoutException;

public class TimeoutAwareExecutor {

    private final long executionTimeout;

    private final PriorityBlockingQueue<TimeoutThread> threads;

    private final Thread supervisorThread;

    public TimeoutAwareExecutor(long executionTimeout) {
        this.executionTimeout = executionTimeout;
        this.threads = new PriorityBlockingQueue<>(11,
                Comparator.comparingLong(TimeoutThread::getTimeout));

        supervisorThread = new Thread(() -> {
            try {
                while (true) {
                    TimeoutThread thread = threads.take(); // wait or take the 1st thread with the lowest timeout
                    while (thread.timeout <= System.currentTimeMillis()) {
                        thread.getThread().stop(); // iterate and stop all threads with timeouts
                        thread = threads.take();
                    }
                    threads.add(thread); // this thread shouldn't be interrupted and will go back to the queue
                    Thread.sleep(10); // sleep 10 ms
                }
            } catch (InterruptedException e) {
                // do nothing
            }
        });
        supervisorThread.start();
    }

    /**
     * Executes a given task in a concurrent environment (usually about 500 client threads)
     * interrupting task execution after timeout.
     * @throws TimeoutException if the task execution hasn't finished within timeout.
     */
    public void execute(Runnable task) throws TimeoutException {
        threads.add(new TimeoutThread(System.currentTimeMillis() + executionTimeout, Thread.currentThread()));
        try {
            task.run();
        } catch (ThreadDeath t) {
            throw new TimeoutException();
        }
    }

    public void shutdown() {
        supervisorThread.interrupt();
    }

    private static class TimeoutThread {

        private long timeout;
        private Thread thread;

        public TimeoutThread(long timeout, Thread thread) {
            this.timeout = timeout;
            this.thread = thread;
        }

        public long getTimeout() {
            return timeout;
        }

        public Thread getThread() {
            return thread;
        }
    }
}
