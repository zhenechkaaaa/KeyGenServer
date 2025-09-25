package ru.nsu.odnostorontseva.keygen;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class KeyGenServer {
    private final int port;
    private final int workers;
    private final BlockingQueue<KeyGen> taskQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<KeyGenRes> resultQueue = new LinkedBlockingQueue<>();


    public KeyGenServer(int port, int workers) {
        this.port = port;
        this.workers = workers;
    }

    public void start() throws IOException {
        Selector selector = Selector.open();

        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server listening on port: " + port);

        for (int i = 0; i < workers; i++) {
            Thread worker = new Thread(new KeyGenWorker(taskQueue, resultQueue));
            worker.start();
        }

        while (true) {
            int ready = selector.select();
            if (ready > 0) {
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectedKeys.iterator();

                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();

                    try {
                        if (key.isAcceptable()) handleAccept(serverSocket, selector);
                        if (key.isReadable()) handleRead(key);
                        if (key.isWritable()) handleWrite(key);
                    } catch (IOException e) {
                        key.cancel();
                        try {
                            key.channel().close();
                        } catch (IOException ignore) {}
                    }
                }
            }

            while (!resultQueue.isEmpty()) {
                KeyGenRes result = resultQueue.poll();
                if (result != null) {
                    ClientContext context = (ClientContext) result.clientKey.attachment();
                    context.writeBuffer = ByteBuffer.wrap(result.resp);
                    result.clientKey.interestOps(SelectionKey.OP_WRITE);
                }
            }
        }
    }

    private void handleAccept(ServerSocketChannel server, Selector selector) throws IOException {
        SocketChannel client = server.accept();
        client.configureBlocking(false);

        ClientContext context = new ClientContext();
        client.register(selector, SelectionKey.OP_READ, context);
        System.out.println("Accepted connection from " + client.getRemoteAddress());
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ClientContext context = (ClientContext) key.attachment();

        int read = client.read(context.inputBuffer);
        if (read == -1) {
            System.out.println("Client " + client.getRemoteAddress() + " disconnected");
            client.close();
            key.cancel();
            return;
        }

        context.inputBuffer.flip();
        while (context.inputBuffer.hasRemaining()) {
            byte b = context.inputBuffer.get();
            if (b == 0) {
                context.clientName = context.sb.toString();
                context.sb.setLength(0);

                System.out.println("Client " + client.getRemoteAddress() +
                        " received name: " + context.clientName);

                taskQueue.add(new KeyGen(context.clientName, key));
                context.waitingForKeys = true;
            } else {
                context.sb.append((char) b);
            }
        }
        context.inputBuffer.clear();
    }

    private void handleWrite(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ClientContext context = (ClientContext) key.attachment();

        if (context.writeBuffer != null) {
            client.write(context.writeBuffer);

            if (!context.writeBuffer.hasRemaining()) {
                System.out.println("Response sent to " + context.clientName);
                client.close();
                key.cancel();
            }
        }
    }
}
