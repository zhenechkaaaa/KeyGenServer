package ru.nsu.odnostorontseva.keygen;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;


public class KeyGenServer {
    private final int port;

    public KeyGenServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        Selector selector = Selector.open();

        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            int ready = selector.select();
            if (ready == 0) continue;

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
                    try { key.channel().close(); } catch (IOException ignore) {}
                }
            }
        }
    }

    private void handleAccept(ServerSocketChannel server, Selector selector) throws IOException {
        SocketChannel client = server.accept();
        client.configureBlocking(false);

        ClientContext context = new ClientContext();
        client.register(selector, SelectionKey.OP_READ, context);
    }

    private void handleRead(SelectionKey key) throws IOException {}
    private void handleWrite(SelectionKey key) throws IOException {}

}
