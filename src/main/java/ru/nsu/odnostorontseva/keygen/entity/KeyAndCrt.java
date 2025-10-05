package ru.nsu.odnostorontseva.keygen.entity;

import lombok.AllArgsConstructor;

import java.security.KeyPair;
import java.security.cert.X509Certificate;

@AllArgsConstructor
public class KeyAndCrt {
    public final KeyPair keyPair;
    public final X509Certificate cert;
}