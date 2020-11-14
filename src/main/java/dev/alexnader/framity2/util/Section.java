package dev.alexnader.framity2.util;

import javax.annotation.Nonnull;
import java.util.Iterator;

public class Section implements Iterable<Integer> {
    private final int start;
    private final int end;

    public Section(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public static Section exclusive(int start, int end) {
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

    public int makeRelative(int absolute) {
        return absolute - start;
    }

    public int makeAbsolute(int relative) {
        return relative + start;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Section section = (Section) o;

        if (start != section.start) return false;
        return end == section.end;
    }

    @Override
    public int hashCode() {
        int result = start;
        result = 31 * result + end;
        return result;
    }

    public boolean contains(int i) {
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
