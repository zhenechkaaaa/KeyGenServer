package ru.nsu.odnostorontseva.client;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class KeyGenClient {
    public static void main(String[] args) throws Exception {
        String[] names = {"Alice", "Bob", "Charlie", "Diana", "Eve", "Frank", "Grace", "Hank", "Ivy", "Jack"};
        Random rand = new Random();
        String name = names[rand.nextInt(names.length)];
        String host = "keygen-server";
        int port = 8080;

        Socket socket = null;
        int retries = 10;

        while (retries > 0) {
            try {
                socket = new Socket(host, port);
                break;
            } catch (IOException e) {
                System.out.println("Сервер ещё не готов, пробуем снова...");
                retries--;
                Thread.sleep(1000);
            }
        }

        if (socket == null) {
            System.err.println("Не удалось подключиться к серверу!");
            System.exit(1);
        }

        try {
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            out.write(name.getBytes(StandardCharsets.US_ASCII));
            out.write(0);
            out.flush();

            byte[] buffer = in.readAllBytes();
            String response = new String(buffer, StandardCharsets.UTF_8);

            String privateKeyPem = extractPemBlock(response, "RSA PRIVATE KEY");
            String certPem = extractPemBlock(response, "CERTIFICATE");

            try (FileOutputStream fos = new FileOutputStream("client.key")) {
                fos.write(privateKeyPem.getBytes(StandardCharsets.UTF_8));
            }

            try (FileOutputStream fos = new FileOutputStream("client.crt")) {
                fos.write(certPem.getBytes(StandardCharsets.UTF_8));
            }

            System.out.println("Private key saved to client.key");
            System.out.println("Certificate saved to client.crt");
        } finally {
            socket.close();
        }
    }

    private static String extractPemBlock(String pem, String type) {
        String beginMarker = "-----BEGIN " + type + "-----";
        String endMarker = "-----END " + type + "-----";
        int begin = pem.indexOf(beginMarker);
        int end = pem.indexOf(endMarker, begin);
        if (begin == -1 || end == -1) {
            throw new IllegalArgumentException(type + " block not found in PEM");
        }
        return pem.substring(begin, end + endMarker.length()) + "\n";
    }
}
