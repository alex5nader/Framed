package dev.alexnader.framed.block.frame.data;

import dev.alexnader.framed.util.Section;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

import static dev.alexnader.framed.Framed.SPECIAL_ITEMS;

public class Sections {
    public static final int BASE_INDEX = 0;
    public static final int OVERLAY_INDEX = 1;
    public static final int SPECIAL_INDEX = 2;

    public static Sections fromTag(final ListTag tag) {
        return new Sections(makeSections(tag.stream().mapToInt(t -> ((IntTag) t).getInt())));
    }

    private static Section[] makeSections(final IntStream sizes) {
        int start = 0;
        final int[] sizeArr = sizes.toArray();
        final Section[] sections = new Section[sizeArr.length];
        for (int i = 0, sizeArrLength = sizeArr.length; i < sizeArrLength; i++) {
            final int size = sizeArr[i];
            sections[i] = Section.exclusive(start, start + size);
            start += size;
        }
        return sections;
    }

    private final Section[] sections;

    public Sections(final Section[] sections) {
        this.sections = sections;
    }

    public Sections(final int partCount, final int... otherSizes) {
        this(makeSections(IntStream.concat(IntStream.of(partCount, partCount, SPECIAL_ITEMS.MAP.size()), Arrays.stream(otherSizes))));
    }

    public Section get(final int index) {
        return sections[index];
    }

    public Section base() {
        return get(BASE_INDEX);
    }

    public Section overlay() {
        return get(OVERLAY_INDEX);
    }

    public Section special() {
        return get(SPECIAL_INDEX);
    }

    public Section itemIndices() {
        return Section.exclusive(0, sections[sections.length - 1].end());
    }

    public boolean containsSlot(final int slot) {
        return 0 <= slot && slot < sections[sections.length - 1].end();
    }

    public int findSectionIndexOf(final int absoluteIndex) {
        for (int i = 0; i < sections.length; i++) {
            if (absoluteIndex < sections[i].end()) {
                return i;
            }
        }
        return -1;
    }

    public Optional<ItemStack>[] makeItems() {
        //noinspection unchecked
        final Optional<ItemStack>[] items = new Optional[sections[sections.length - 1].end()];
        Arrays.fill(items, Optional.empty());
        return items;
    }

    public Optional<BlockState>[] makeBaseStates() {
        //noinspection unchecked
        final Optional<BlockState>[] baseStates = new Optional[base().size()];
        Arrays.fill(baseStates, Optional.empty());
        return baseStates;
    }

    public ListTag toTag() {
        final ListTag tag = new ListTag();

        for (final Section section : sections) {
            tag.add(IntTag.of(section.size()));
        }

        return tag;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Sections sections1 = (Sections) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(sections, sections1.sections);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(sections);
    }
}
