package ru.nsu.odnostorontseva.keygen;

import java.nio.channels.Selector;
import java.util.concurrent.BlockingQueue;

public class KeyGenWorker implements Runnable {
    private final BlockingQueue<KeyGen> taskQueue;
    private final BlockingQueue<KeyGenRes> resultQueue;
    private final KeyGenService keyGenService;
    private final Selector selector;


    public KeyGenWorker(BlockingQueue<KeyGen> taskQueue,
                        BlockingQueue<KeyGenRes> resultQueue, KeyGenService keyGenService, Selector selector) {
        this.taskQueue = taskQueue;
        this.resultQueue = resultQueue;
        this.keyGenService = keyGenService;
        this.selector = selector;

    }

    @Override
    public void run() {
        try {
            while (true) {
                KeyGen task = taskQueue.take();

                KeyAndCrt keyAndCrt = keyGenService.generate(task.name);
                byte[] response = keyGenService.serialize(keyAndCrt);

                KeyGenRes result = new KeyGenRes(task.clientKey, response);
                resultQueue.put(result);
                selector.wakeup();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
