package ru.nsu.odnostorontseva.keygen;

import lombok.AllArgsConstructor;

import java.nio.channels.SelectionKey;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
public class KeyGen {
    public final String name;
    public final CompletableFuture<byte[]> future;
}
