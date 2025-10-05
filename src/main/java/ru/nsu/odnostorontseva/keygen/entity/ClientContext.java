package ru.nsu.odnostorontseva.keygen.entity;

import java.nio.ByteBuffer;

public class ClientContext {
    public ByteBuffer inputBuffer = ByteBuffer.allocate(1024);
    public final StringBuilder sb = new StringBuilder();
    public String clientName = null;
    public ByteBuffer writeBuffer = null;
}
