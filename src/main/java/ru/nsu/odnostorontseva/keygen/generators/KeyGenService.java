package ru.nsu.odnostorontseva.keygen.generators;

import ru.nsu.odnostorontseva.keygen.entity.KeyAndCrt;

public interface KeyGenService {
    KeyAndCrt generate(String clientName) throws Exception;
    byte[] serialize(KeyAndCrt kp) throws Exception;
}

