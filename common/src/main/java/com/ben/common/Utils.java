package com.ben.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Utils {

    public static void handleRawFile(String filePath) throws IOException {
        String[] fileNameParts = filePath.split("/");

        String fileName = fileNameParts[fileNameParts.length - 1];

        String pathToFile = (fileNameParts.length > 1)
                ? "/" + filePath.replace("/" + fileName, "") : "";

        Path pathToNewFolder = Paths.get("output/" + pathToFile);

        Files.createDirectories(pathToNewFolder);
    }

    public static String convertToUpperHyphen(String input) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                result.append("-");
            }
            result.append(c);
        }

        return result.toString().toUpperCase();
    }

    public static String convertToTitleCase(String input) {
        return Arrays.stream(input.split("-"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

}
