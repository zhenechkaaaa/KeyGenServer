package ru.nsu.odnostorontseva.keygen;

import lombok.AllArgsConstructor;
import ru.nsu.odnostorontseva.keygen.entity.ClientContext;
import ru.nsu.odnostorontseva.keygen.entity.KeyGen;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
public class ClientHandler {
    private final BlockingQueue<KeyGen> taskQueue;
    private final Map<String, CompletableFuture<byte[]>> generationMap;

    public void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ClientContext context = (ClientContext) key.attachment();

        if (channel.read(context.inputBuffer) == -1) {
            System.out.println("Client " + channel.getRemoteAddress() + " disconnected");
            channel.close();
            key.cancel();
            return;
        }

        context.inputBuffer.flip();
        while (context.inputBuffer.hasRemaining()) {
            byte b = context.inputBuffer.get();
            if (b == 0) {
                context.clientName = context.sb.toString();
                context.sb.setLength(0);

                System.out.println("Client " + channel.getRemoteAddress() +
                        " received name: " + context.clientName);

                submitGeneration(key, context);
                break;
            } else {
                context.sb.append((char) b);
            }
        }
        context.inputBuffer.compact();
    }

    private void submitGeneration(SelectionKey key, ClientContext ctx) {
        var future = generationMap.computeIfAbsent(ctx.clientName, n -> {
            var f = new CompletableFuture<byte[]>();
            taskQueue.add(new KeyGen(n, f));
            return f;
        });

        future.whenComplete((bytes, ex) -> {
            if (ex != null) {
                System.err.println("Generation failed for " + ctx.clientName + ": " + ex);
                try {
                    close(key);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return;
            }
            ctx.writeBuffer = ByteBuffer.wrap(bytes);
            enableWrite(key);
        });
    }

    private void enableWrite(SelectionKey key) {
        try {
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
            key.selector().wakeup();
        } catch (CancelledKeyException ignored) {}
    }

    private void close(SelectionKey key) throws IOException {
        key.cancel();
        key.channel().close();
    }


    public void write(SelectionKey key) throws IOException {
        SocketChannel ch = (SocketChannel) key.channel();
        ClientContext ctx = (ClientContext) key.attachment();

        if (ctx.writeBuffer != null) {
            ch.write(ctx.writeBuffer);
            if (!ctx.writeBuffer.hasRemaining()) {
                System.out.println("Response sent to " + ctx.clientName);
                ch.close();
                key.cancel();
            }
        }
    }
}
