package com.bot;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class ReadFile {
    public static List<String> readFileInList(File file) {

        List<String> lines = Collections.emptyList();

        try {
            lines = Files.readAllLines(Paths.get(file.toString()), StandardCharsets.UTF_8);
        }

        catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    public static String readFileInString(File file) {

        byte[] bytes;
        String result = "";

        try {
            bytes = Files.readAllBytes(Paths.get(file.toString()));
            result = new String(bytes, "UTF-8");
        }

        catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}