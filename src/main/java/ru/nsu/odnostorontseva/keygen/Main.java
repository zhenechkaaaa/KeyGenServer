package ru.nsu.odnostorontseva.keygen;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the num of the workers: ");
        int workers = scanner.nextInt();
        scanner.close();

        Security.addProvider(new BouncyCastleProvider());

        byte[] keyBytes;
        try (InputStream is = Main.class.getClassLoader().getResourceAsStream("ca-pkcs8.key")) {
            if (is == null) {
                throw new RuntimeException("Resource not found");
            }
            keyBytes = is.readAllBytes();
        }

        String pem = new String(keyBytes, StandardCharsets.US_ASCII)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = java.util.Base64.getDecoder().decode(pem);

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        PrivateKey caPrivateKey = KeyFactory.getInstance("RSA").generatePrivate(spec);

        RSAKeyGenService keyGenService = new RSAKeyGenService(caPrivateKey, "MyCA");

        KeyGenServer server = new KeyGenServer(8080, workers, keyGenService);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nCtrl+C detected, shutting down server...");
            server.stop();
        }));

        server.start();
    }
}
