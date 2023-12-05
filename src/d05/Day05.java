package d05;

import util.PreconditionUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day05 {
    private static final Pattern numberPattern = Pattern.compile("(\\d+)");
    private static final Pattern mapStartPattern = Pattern.compile("^(.*)-to-(.*) map:$");
    private static final String SEED = "seed";

    private record Conversion(Range source, Range dest) {
        public boolean isInRange(long sourceValue) {
            // start = 0, length = 2 covers [ 0, 1 ]
            long diff = sourceValue - source.start();
            return diff >= 0 && diff < source.length();
        }

        // precondition: isInRange(sourceValue) is true
        public long calculateValue(long sourceValue) {
            PreconditionUtil.assertTrue(isInRange(sourceValue));
            return dest.start() + (sourceValue - source.start());
        }

        // precondition: source.covers(range) is true
        public Range convert(Range range) {
            PreconditionUtil.assertTrue(source.covers(range));
            long diff = range.start() - source.start();
            return new Range(dest.start() + diff, range.length());
        }
    }
    private record Target(String name, List<Conversion> conversions) {}

    private static List<Long> parseNumberList(String line) {
        Matcher m = numberPattern.matcher(line);
        List<Long> result = new ArrayList<>();
        while (m.find()) {
            result.add(Long.parseLong(m.group(1)));
        }
        return result;
    }

    enum ParseState {
        EMPTY,
        IN
    }

    private static class MapEntryBuilder {
        String target = null;
        String source = null;
        final List<Conversion> conversions = new ArrayList<>();

        MapEntryBuilder setSource(String source) {
            this.source = source;
            return this;
        }

        MapEntryBuilder setTarget(String target) {
            this.target = target;
            return this;
        }

        MapEntryBuilder addConversion(Conversion conversion) {
            this.conversions.add(conversion);
            return this;
        }

        void build(Map<String, Target> destMap) {
            destMap.put(source, new Target(target, conversions));
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("src/d05/in.txt"));
        List<String> lines = reader.lines().toList();
        List<Long> seeds = parseNumberList(lines.get(0));

        Map<String, Target> sourceToTargetMap = parseConversionMap(lines);
        long minLocation = partOne(seeds, sourceToTargetMap);
        System.out.println("P1: " + minLocation);

        List<Range> seedRanges = convertSeedRanges(seeds);
        minLocation = partTwo(seedRanges, sourceToTargetMap);
        System.out.println("P2: " + minLocation);
    }

    private static long partTwo(List<Range> seedRanges, Map<String, Target> sourceToTargetMap) {
        long minLocation = Long.MAX_VALUE;
        for (Range range : seedRanges) {
            List<Range> converted = convertToMinLocation(range, sourceToTargetMap);
            for (Range r : converted) {
                if (r.start() < minLocation) {
                    minLocation = r.start();
                }
            }
        }
        return minLocation;
    }

    // Convert seed range to location range(s)
    private static List<Range> convertToMinLocation(Range range, Map<String, Target> sourceToTargetMap) {
        List<Range> result = List.of(range);
        Target target = sourceToTargetMap.get(SEED);
        while (target != null) {
            result = applyConversion(target.conversions(), result);
            target = sourceToTargetMap.get(target.name);
        }
        return result;
    }

    private static List<Range> applyConversion(List<Conversion> conversions, List<Range> ranges) {
        List<Range> toProcess = new ArrayList<>(ranges);
        List<Range> result = new ArrayList<>();
        for (Conversion conversion : conversions) {
            List<Range> toRemove = new ArrayList<>();
            List<Range> toAdd = new ArrayList<>();
            for (Range current : toProcess) {
                Range overlap = conversion.source().overlap(current);
                if (! overlap.isEmpty()) {
                    // there is some overlap with this conversion
                    // convert the overlapping part and add it to result
                    result.add(conversion.convert(overlap));
                    // extract all ranges that didn't overlap and continue processing
                    toAdd.addAll(current.minus(overlap));
                    toRemove.add(current);
                }
            }
            toProcess.removeAll(toRemove);
            toProcess.addAll(toAdd);
        }
        result.addAll(toProcess);
        return result;
    }

    private static List<Range> convertSeedRanges(List<Long> seeds) {
        PreconditionUtil.assertEquals(seeds.size() % 2,0);
        List<Range> result = new ArrayList<>();
        for (int i = 0; i < seeds.size(); i+= 2) {
            result.add(new Range(seeds.get(i), seeds.get(i + 1)));
        }
        return result;
    }

    private static long partOne(List<Long> seeds, Map<String, Target> sourceToTargetMap) {
        long minLocation = Long.MAX_VALUE;
        for (long seed : seeds) {
            long location = convert(seed, SEED, sourceToTargetMap);
            if (location < minLocation) {
                minLocation = location;
            }
        }
        return minLocation;
    }

    // Convert until there are no targets left
    private static long convert(long sourceValue, String start, Map<String, Target> sourceToTargetMap) {
        Target target = sourceToTargetMap.get(start);
        if (target == null) {
            return sourceValue;
        }
        long convertedValue = applyConversions(sourceValue, target.conversions());
        return convert(convertedValue, target.name, sourceToTargetMap);
    }

    private static long applyConversions(long value, List<Conversion> conversions) {
        // iterate over conversions, check if there is a range match, then apply conversion
        for (Conversion conversion : conversions) {
            if (conversion.isInRange(value)) {
                return conversion.calculateValue(value);
            }
        }
        // otherwise: return value
        return value;
    }

    private static void parseEmpty(String line, MapEntryBuilder builder) {
        Matcher m = mapStartPattern.matcher(line);
        PreconditionUtil.assertTrue(m.find());
        String source = m.group(1);
        String target = m.group(2);
        builder.setSource(source).setTarget(target);
    }

    private static void parseConversion(String line, MapEntryBuilder builder) {
        List<Long> numbers = parseNumberList(line);
        PreconditionUtil.assertEquals(numbers.size(), 3);
        long length = numbers.get(2);
        builder.addConversion(new Conversion(new Range(numbers.get(1), length), new Range(numbers.get(0), numbers.get(2))));
    }

    private static Map<String, Target> parseConversionMap(List<String> lines) {
        ParseState parseState = ParseState.EMPTY;
        Map<String, Target> sourceToTargetMap = new HashMap<>();
        MapEntryBuilder builder = null;
        for (String line : lines.subList(2, lines.size())) {
            switch (parseState) {
                case EMPTY -> {
                    builder = new MapEntryBuilder();
                    parseEmpty(line, builder);
                    parseState = ParseState.IN;
                }
                case IN -> {
                    if (line.isEmpty()) {
                        // delimit
                        builder.build(sourceToTargetMap);
                        builder = null;
                        parseState = ParseState.EMPTY;
                    } else {
                        PreconditionUtil.assertNotNull(builder);
                        parseConversion(line, builder);
                    }
                }
            }
        }
        if (builder != null) {
            builder.build(sourceToTargetMap);
        }
        return sourceToTargetMap;
    }
}
