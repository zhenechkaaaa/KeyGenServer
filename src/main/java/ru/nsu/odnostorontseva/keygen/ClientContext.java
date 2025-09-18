package ru.nsu.odnostorontseva.keygen;

import java.nio.ByteBuffer;

public class ClientContext {
    ByteBuffer inputBuffer = ByteBuffer.allocate(1024);
    ByteBuffer outputBuffer;
    String clientName;

    /*
    * // Дополнительно для RSA
    boolean requestInProgress = false;
    KeyPair keyPair;
    X509Certificate certificate;*/
}
