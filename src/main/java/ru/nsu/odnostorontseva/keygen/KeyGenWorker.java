package ru.nsu.odnostorontseva.keygen;

import java.util.concurrent.BlockingQueue;

public class KeyGenWorker implements Runnable {
    private final BlockingQueue<KeyGen> taskQueue;
    private final BlockingQueue<KeyGenRes> resultQueue;

    public KeyGenWorker(BlockingQueue<KeyGen> taskQueue,
                        BlockingQueue<KeyGenRes> resultQueue) {
        this.taskQueue = taskQueue;
        this.resultQueue = resultQueue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                KeyGen task = taskQueue.take();

                /// ////////////////////////////////////////////////////////
                String response = "Keys for " + task.name + "\n";
                KeyGenRes result = new KeyGenRes(task.clientKey, response.getBytes());
                resultQueue.put(result);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
