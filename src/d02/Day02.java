package d02;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day02 {
    private static final Pattern linePattern = Pattern.compile("^Game (\\d+): (.*)$");
    private static final Pattern colorPattern = Pattern.compile("^(\\d+) (red|blue|green)$");

    // P1 values
    private static final int MAX_RED = 12;
    private static final int MAX_GREEN = 13;
    private static final int MAX_BLUE = 14;

    private static void AssertTrue(boolean expr) {
        if (!expr) {
            throw new RuntimeException("expression was false");
        }
    }

    private record CubeSet(int red, int green, int blue) {}

    private static List<CubeSet> parseCubes(String rawCubes) {
        String[] sets = rawCubes.split("; "); // [ (1 blue, 2 red), (2 green, 4 blue), ... ]
        List<CubeSet> result = new ArrayList<>();
        for (String set : sets) {
            String[] colors = set.split(", "); // [ (1 blue), (2 red) ]
            int red = 0;
            int blue = 0;
            int green = 0;
            for (String color : colors) {
                Matcher m = colorPattern.matcher(color);
                AssertTrue(m.find());
                int amount = Integer.parseInt(m.group(1));
                String colorString = m.group(2);
                switch (colorString) {
                    case "red" -> red = amount;
                    case "blue" -> blue = amount;
                    case "green" -> green = amount;
                    default -> throw new RuntimeException("unrecognized color: " + colorString + ", in color " + color);
                }
            }
            result.add(new CubeSet(red, green, blue));
        }
        return result;
    }

    private static boolean isPossibleP1(List<CubeSet> cubeSets) {
        for (CubeSet cubeSet : cubeSets) {
            if (cubeSet.red > MAX_RED || cubeSet.blue > MAX_BLUE || cubeSet.green > MAX_GREEN) {
                return false;
            }
        }
        return true;
    }

    private static CubeSet minimumCubeSet(List<CubeSet> cubeSets) {
        int minRed = 0;
        int minBlue = 0;
        int minGreen = 0;
        for (CubeSet cubeSet : cubeSets) {
            minGreen = Math.max(minGreen, cubeSet.green);
            minBlue = Math.max(minBlue, cubeSet.blue);
            minRed = Math.max(minRed, cubeSet.red);
        }
        return new CubeSet(minRed, minGreen, minBlue);
    }

    private static long calculatePower(List<CubeSet> game) {
        CubeSet minimumValues = minimumCubeSet(game);
        // what if green or red or blue is 0? is the power then 0? Logically: yes
        return (long)minimumValues.green * minimumValues.red * minimumValues.blue;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("src/d02/in.txt"));

        List<String> lines = reader.lines().toList();

        int possibleIdSum = 0;
        long powerSum = 0;
        for (String line : lines) {
            Matcher m = linePattern.matcher(line);
            AssertTrue(m.find());
            int gameId = Integer.parseInt(m.group(1));
            String cubes = m.group(2);
            List<CubeSet> cubeSets = parseCubes(cubes);

            powerSum += calculatePower(cubeSets);
            if (isPossibleP1(cubeSets)) {
                possibleIdSum += gameId;
            }
        }
        System.out.println("P1: " + possibleIdSum);
        System.out.println("P2: " + powerSum);
    }
}
