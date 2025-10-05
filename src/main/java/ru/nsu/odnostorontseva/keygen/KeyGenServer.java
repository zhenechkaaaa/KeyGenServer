package ru.nsu.odnostorontseva.keygen;

import ru.nsu.odnostorontseva.keygen.entity.ClientContext;
import ru.nsu.odnostorontseva.keygen.entity.KeyGen;
import ru.nsu.odnostorontseva.keygen.generators.KeyGenService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class KeyGenServer {
    private final int port;
    private final int workers;
    private final KeyGenService keyGenService;

    public KeyGenServer(int port, int workers, KeyGenService keyGenService) {
        this.port = port;
        this.workers = workers;
        this.keyGenService = keyGenService;
    }

    private volatile boolean running = true;
    private volatile Selector selector;

    private final BlockingQueue<KeyGen> taskQueue = new LinkedBlockingQueue<>();
    private final Map<String, CompletableFuture<byte[]>> generationMap = new ConcurrentHashMap<>();
    private final List<Thread> workerThreads = new ArrayList<>();

    public void start() throws IOException {
        selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server listening on port: " + port);

        for (int i = 0; i < workers; i++) {
            Thread worker = new Thread(new KeyGenWorker(taskQueue, keyGenService));
            worker.start();
            workerThreads.add(worker);
        }

        ClientHandler clientHandler = new ClientHandler(taskQueue, generationMap);

        try {
            while (running) {
                selector.select();

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectedKeys.iterator();

                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();

                    try {
                        if (!key.isValid()) continue;

                        if (key.isAcceptable()) handleAccept(serverSocket, selector);
                        else if (key.isReadable()) clientHandler.read(key);
                        else if (key.isWritable()) clientHandler.write(key);
                    } catch (IOException e) {
                        key.cancel();
                        key.channel().close();
                    }
                }
            }
        } finally {
            for (Thread worker : workerThreads) {
                worker.interrupt();
            }

            for (Thread worker : workerThreads) {
                try {
                    worker.join();
                } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            }

            serverSocket.close();
            selector.close();
            System.out.println("Server shutdown complete.");

        }
    }

    public void stop() {
        running = false;
        if (selector != null) selector.wakeup();
    }

    private void handleAccept(ServerSocketChannel server, Selector selector) throws IOException {
        SocketChannel client = server.accept();
        client.configureBlocking(false);

        ClientContext context = new ClientContext();
        client.register(selector, SelectionKey.OP_READ, context);
        System.out.println("Accepted connection from " + client.getRemoteAddress());
    }
}
