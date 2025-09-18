package ru.nsu.odnostorontseva.keygen;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        new KeyGenServer(8080).start();
    }
}