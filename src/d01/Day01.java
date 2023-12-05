package d01;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day01 {

//    private static final Pattern digitPattern = Pattern.compile("([0-9])");
    private static final Pattern digitPattern = Pattern.compile("(one|two|three|four|five|six|seven|eight|nine|[1-9])");

    private static int convert(String match) {
        Map<String, Integer> digitMap = Map.of(
                "one", 1,
                "two", 2,
                "three", 3,
                "four", 4,
                "five", 5,
                "six", 6,
                "seven", 7,
                "eight", 8,
                "nine", 9
        );

        Integer result = digitMap.get(match);
        if (result == null) {
            return Integer.parseInt(match);
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("src/d01/in.txt"));
        String line = reader.readLine();
        long sum = 0;
        while (line != null) {
            List<String> numbers = new ArrayList<>();
            Matcher m = digitPattern.matcher(line);

            for (int i = 0; i < line.length(); i++) {
                if (m.find(i)) {
                    numbers.add(m.group(1));
                }
            }
            String a = numbers.get(0);
            String b = numbers.get(numbers.size() - 1);
            int result = (10 * convert(a)) + convert(b);
            sum += result;
            line = reader.readLine();
        }
        System.out.println(sum);
    }
}