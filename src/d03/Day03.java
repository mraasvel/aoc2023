package d03;

import util.Point;
import util.PreconditionUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day03 {
    private static final Pattern numberPattern = Pattern.compile("(\\d+)");
    private static final Map<Point, List<PartNumber>> gearToNumbers = new HashMap<>();

    private record CharPoint(Character ch, Point point) {}

    // Returns list of [ above, point, below ], with `null` value if it is not present
    private static List<CharPoint> getThreeChars(Point point, List<String> input) {
        List<CharPoint> result = new ArrayList<>();
        result.add(getCharAt(new Point(point.x(), point.y() - 1), input));
        result.add(getCharAt(point, input));
        result.add(getCharAt(new Point(point.x(), point.y() + 1), input));
        return result;
    }

    // null if char not present
    private static CharPoint getCharAt(Point point, List<String> input) {
        try {
            return new CharPoint(input.get(point.y()).charAt(point.x()), point);
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isSymbol(CharPoint ch) {
        if (ch == null) {
            return false;
        }
        // Is a digit a symbol?
        return ch.ch() != '.' && !Character.isDigit(ch.ch());
    }

    private static boolean hasAdjacentSymbol(PartNumber partNumber, List<String> input) {
        int x = partNumber.start().x();
        int y = partNumber.start().y();
        List<CharPoint> adjacentChars = new ArrayList<>();
        for (int i = 0; i < partNumber.length(); i++) {
            List<CharPoint> chars = getThreeChars(new Point(x + i, y), input);
            adjacentChars.add(chars.get(0));
            adjacentChars.add(chars.get(2));
        }
        // add all three chars that are to the left and right of number
        adjacentChars.addAll(getThreeChars(new Point(x - 1, y), input));
        adjacentChars.addAll(getThreeChars(new Point(x + partNumber.length(), y), input));
        boolean result = false;
        for (CharPoint ch : adjacentChars) {
            if (isSymbol(ch)) {
                result = true;

                // P2: add to gearToNumbers to determine which numbers are adjacent to gears later on
                if (ch.ch() == '*') {
                    List<PartNumber> partNumbers = gearToNumbers.getOrDefault(ch.point(), new ArrayList<>());
                    partNumbers.add(partNumber);
                    gearToNumbers.put(ch.point(), partNumbers);
                }
            }
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("src/d03/in.txt"));
        List<String> lines = reader.lines().toList();
        List<PartNumber> numbers = new ArrayList<>();
        for (int y = 0; y < lines.size(); y++) {
            String line = lines.get(y);
            Matcher matcher = numberPattern.matcher(line);
            // numbers don't overlap to the next line, so we can safely use regex
            while (matcher.find()) {
                int x = matcher.start();
                int length = matcher.end() - x;
                String numberString = matcher.group(1);
                PreconditionUtil.assertEquals(numberString.length(), length);
                int value = Integer.parseInt(matcher.group(1));
                numbers.add(new PartNumber(new Point(x, y), value, length));
            }
        }

        // p1 also populates map for p2
        Optional<Integer> p1 = numbers.stream()
                .filter(partNumber -> hasAdjacentSymbol(partNumber, lines))
                .map(PartNumber::value)
                .reduce(Integer::sum);
        PreconditionUtil.assertTrue(p1.isPresent());
        System.out.println("P1: " + p1.get());

        Optional<Integer> p2 = gearToNumbers.values()
                .stream()
                .filter(partNumbers -> partNumbers.size() == 2)
                .map(partNumbers -> partNumbers.get(0).value() * partNumbers.get(1).value())
                .reduce(Integer::sum);
        PreconditionUtil.assertTrue(p2.isPresent());
        System.out.println("P2: " + p2.get());
    }
}
