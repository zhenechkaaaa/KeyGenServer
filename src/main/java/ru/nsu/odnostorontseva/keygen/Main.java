package ru.nsu.odnostorontseva.keygen;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the num of the workers: ");
        int workers = scanner.nextInt();
        scanner.close();
        new KeyGenServer(8080, workers).start();
    }
}