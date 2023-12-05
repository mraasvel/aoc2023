package d05;

import java.util.List;
import java.util.stream.Stream;

public record Range(long start, long length) {
    public static final Range EMPTY = new Range(0, 0);

    // returns overlapping range, or EMPTY if there is no overlap
    public Range overlap(Range rhs) {
        long maxStart = Long.max(start(), rhs.start());
        long minEnd = Long.min(end(), rhs.end());
        if (maxStart >= minEnd) {
            // no overlap
            return Range.EMPTY;
        }
        return new Range(maxStart, minEnd - maxStart);
    }

    public List<Range> minus(Range rhs) {
        Range overlap = overlap(rhs);
        if (overlap.isEmpty()) {
            return List.of(new Range(start(), length()));
        } else {
            return Stream.of(
                    new Range(start(), overlap.start() - start()),
                    new Range(overlap.end(), end() - overlap.end())
            ).filter(r -> !r.isEmpty()).toList();
        }
    }

    public boolean isEmpty() {
        return length <= 0;
    }

    public boolean covers(Range other) {
        return start() <= other.start() && end() >= other.end();
    }

    public long end() {
        return start + length;
    }
}
