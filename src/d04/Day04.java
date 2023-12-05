package d04;

import util.PreconditionUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day04 {
    private static final Pattern numberPattern = Pattern.compile("(\\d+)");
    private static final Map<Integer, Integer> multiplier = new HashMap<>();

    private static List<Integer> extractNumbers(String s) {
        List<Integer> result = new ArrayList<>();
        Matcher m = numberPattern.matcher(s);
        while (m.find()) {
            result.add(Integer.parseInt(m.group(1)));
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("src/d04/in.txt"));
        List<String> lines = reader.lines().toList();
        int partOne = 0;
        for (String line : lines) {
            String[] cardParts = line.split(":");
            String[] parts = cardParts[1].split("\\|");
            Set<Integer> winningNumbers = Set.copyOf(extractNumbers(parts[0]));
            List<Integer> drawnNumbers = extractNumbers(parts[1]);

            int id = getId(cardParts);
            int amount = multiplier.getOrDefault(id, 1);
            int matches = totalMatches(drawnNumbers, winningNumbers);

            // Card <id> has <amount> instances
            // The next <matches> cards are increased by <amount> with a default of 1
            for (int i = 0; i < matches; i++) {
                int index = id + i + 1;
                multiplier.put(index, multiplier.getOrDefault(index, 1) + amount);
            }

            int points = partOnePoints(drawnNumbers, winningNumbers);
            partOne = Math.addExact(partOne, points);
        }
        System.out.println("P1: " + partOne);
        Optional<Integer> partTwo = multiplier.values().stream().reduce(Integer::sum);
        PreconditionUtil.assertTrue(partTwo.isPresent());
        System.out.println("P2: " + partTwo.get());
    }

    private static int getId(String[] cardParts) {
        Matcher m = numberPattern.matcher(cardParts[0]);
        PreconditionUtil.assertTrue(m.find());
        int id = Integer.parseInt(m.group(1));
        // every ID needs to be present for final calculation
        if (!multiplier.containsKey(id)) {
            multiplier.put(id, 1);
        }
        return id;
    }

    private static int partOnePoints(List<Integer> drawnNumbers, Set<Integer> winningNumbers) {
        int matches = totalMatches(drawnNumbers, winningNumbers);
        if (matches == 0) {
            return 0;
        }
        return 1 << (matches - 1);
    }

    private static int totalMatches(List<Integer> drawnNumbers, Set<Integer> winningNumbers) {
        return (int) drawnNumbers.stream().filter(winningNumbers::contains).count();
    }

}
