package ru.nsu.odnostorontseva.keygen;

public interface KeyGenService {
    KeyAndCrt generate(String clientName) throws Exception;
    byte[] serialize(KeyAndCrt kp) throws Exception;
}

