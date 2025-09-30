package ru.nsu.odnostorontseva.keygen;

import java.security.KeyPair;
import java.security.cert.X509Certificate;

public class KeyAndCrt {
    final KeyPair keyPair;
    final X509Certificate cert;

    KeyAndCrt(KeyPair keyPair, X509Certificate cert) {
        this.keyPair = keyPair;
        this.cert = cert;
    }
}