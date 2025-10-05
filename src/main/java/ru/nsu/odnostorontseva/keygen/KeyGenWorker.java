package ru.nsu.odnostorontseva.keygen;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import java.util.concurrent.BlockingQueue;

@AllArgsConstructor
public class KeyGenWorker implements Runnable {

    @NonNull
    private final BlockingQueue<KeyGen> taskQueue;
    private final KeyGenService keyGenService;

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                KeyGen task = taskQueue.take();

                try {
                    KeyAndCrt keyAndCrt = keyGenService.generate(task.name);
                    byte[] response = keyGenService.serialize(keyAndCrt);

                    task.future.complete(response);

                } catch (Exception e) {
                    task.future.completeExceptionally(e);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
