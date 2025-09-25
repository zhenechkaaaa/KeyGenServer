package ru.nsu.odnostorontseva.keygen;

import java.nio.channels.SelectionKey;

public class KeyGen {
    final String name;
    final SelectionKey clientKey;

    public KeyGen(String name, SelectionKey clientKey) {
        this.name = name;
        this.clientKey = clientKey;
    }
}
