package com.company.jaimes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        String filename = args[0];
        System.out.println("attempting to read file " + filename);
        NodeController controller = new NodeController(filename);
    }
}
