package ru.nsu.odnostorontseva.keygen;

import java.nio.channels.SelectionKey;

public class KeyGenRes {
    final SelectionKey clientKey;
    final byte[] resp;

    public KeyGenRes(SelectionKey clientKey, byte[] resp) {
        this.clientKey = clientKey;
        this.resp = resp;
    }
}
