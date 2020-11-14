package dev.alexnader.framity2.util;

import javax.annotation.Nonnull;
import java.util.Iterator;

public class Section implements Iterable<Integer> {
    private final int start;
    private final int end;

    public Section(final int start, final int end) {
        this.start = start;
        this.end = end;
    }

    public static Section exclusive(final int start, final int end) {
        return new Section(start, end);
    }

    public int start() {
        return start;
    }

    public int end() {
        return end;
    }

    public int size() {
        return end - start;
    }

    public int makeRelative(final int absolute) {
        return absolute - start;
    }

    public int makeAbsolute(final int relative) {
        return relative + start;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Section section = (Section) o;

        if (start != section.start) return false;
        return end == section.end;
    }

    @Override
    public int hashCode() {
        int result = start;
        result = 31 * result + end;
        return result;
    }

    public boolean contains(final int i) {
        return start <= i && i < end;
    }

    @Nonnull
    @Override
    public Iterator<Integer> iterator() {
        return new SectionIterator();
    }

    private class SectionIterator implements Iterator<Integer> {
        int current = start;

        @Override
        public boolean hasNext() {
            return current < end;
        }

        @Override
        public Integer next() {
            return current++;
        }
    }
}
