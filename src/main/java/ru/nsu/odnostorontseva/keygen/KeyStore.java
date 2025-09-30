package ru.nsu.odnostorontseva.keygen;

import java.util.concurrent.ConcurrentHashMap;

public class KeyStore {
    private static final ConcurrentHashMap<String, KeyAndCrt> storage = new ConcurrentHashMap<>();

    public static KeyAndCrt get(String name) {
        return storage.get(name);
    }

    public static void put(String name, KeyAndCrt pair) {
        storage.put(name, pair);
    }
}
