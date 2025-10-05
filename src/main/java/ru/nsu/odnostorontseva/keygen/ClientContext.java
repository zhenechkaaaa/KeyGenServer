package ru.nsu.odnostorontseva.keygen;

import java.nio.ByteBuffer;

public class ClientContext {
    ByteBuffer inputBuffer = ByteBuffer.allocate(1024);
    final StringBuilder sb = new StringBuilder();
    String clientName = null;
    ByteBuffer writeBuffer = null;
}
