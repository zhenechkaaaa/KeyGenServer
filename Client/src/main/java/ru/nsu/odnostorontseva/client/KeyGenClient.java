package ru.nsu.odnostorontseva.client;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class KeyGenClient {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter host: ");
        String host = scanner.nextLine();

        System.out.print("Enter port: ");
        int port = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter name: ");
        String name = scanner.nextLine();

        try (Socket socket = new Socket(host, port)) {
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            // Отправляем имя
            out.write(name.getBytes(StandardCharsets.UTF_8));
            out.write(0);
            out.flush();

            byte[] buffer = in.readAllBytes();
            String response = new String(buffer, StandardCharsets.UTF_8);
            
            String privateKeyPem = extractPemBlock(response, "RSA PRIVATE KEY");
            String certPem = extractPemBlock(response, "CERTIFICATE");

            // Сохраняем в отдельные файлы
            try (FileOutputStream fos = new FileOutputStream("client.key")) {
                fos.write(privateKeyPem.getBytes(StandardCharsets.UTF_8));
            }

            try (FileOutputStream fos = new FileOutputStream("client.crt")) {
                fos.write(certPem.getBytes(StandardCharsets.UTF_8));
            }

            System.out.println("Private key saved to client.key");
            System.out.println("Certificate saved to client.crt");
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
