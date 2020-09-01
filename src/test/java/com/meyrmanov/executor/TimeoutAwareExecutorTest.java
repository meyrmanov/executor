package com.meyrmanov.executor;


import org.junit.Test;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertTrue;

public class TimeoutAwareExecutorTest {

    /**
     * Simple test which starts 700 threads with random duration (1 - 300ms)
     */
    @Test
    public void testTimeout() throws InterruptedException {
        long executionTimeout = 100;
        TimeoutAwareExecutor executor = new TimeoutAwareExecutor(executionTimeout);
        for (int i = 0; i < 700; i++) {
            try {
                Thread.sleep((long) (Math.random() * 5));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            runThread(executor, (long) (Math.random() * 300), executionTimeout);
        }

        Thread.sleep(4000);
        executor.shutdown();
    }

    private void runThread(TimeoutAwareExecutor executor, long duration, long timeout) {
        new Thread(() -> {
            long now = System.currentTimeMillis();
            try {
                executor.execute(() -> {
                    try {
                        Thread.sleep(duration);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            } catch (TimeoutException e) {
                // do nothing for our test
            }
            long diff = System.currentTimeMillis() - now;
            assertTrue(diff - timeout < 30); // we can't guarantee exact 100ms for execution

        }).start();
    }
}
