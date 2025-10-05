package ru.nsu.odnostorontseva.keygen.entity;

import lombok.AllArgsConstructor;

import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
public class KeyGen {
    public final String name;
    public final CompletableFuture<byte[]> future;
}
