package dev.alexnader.framity2.items;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Pair;
import dev.alexnader.framity2.block.entity.FrameBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static dev.alexnader.framity2.Framity2.PROPERTIES;

public class SpecialItems {
    public static class SpecialItem {
        private final int offset;
        private final BooleanProperty property;

        public SpecialItem(final int offset, final BooleanProperty property) {
            this.offset = offset;
            this.property = property;
        }

        public int offset() {
            return offset;
        }

        public void onAdd(final World world, final FrameBlockEntity frame) {
            world.setBlockState(frame.getPos(), world.getBlockState(frame.getPos()).with(property, true));
        }

        public void onRemove(final World world, final FrameBlockEntity frame) {
            world.setBlockState(frame.getPos(), world.getBlockState(frame.getPos()).with(property, false));
        }
    }

    public final Map<Item, SpecialItem> MAP;

    {
        final List<Pair<Item, BooleanProperty>> pairs = Lists.newArrayList(
            Pair.of(Items.GLOWSTONE_DUST, Properties.LIT),
            Pair.of(Items.REDSTONE, PROPERTIES.HAS_REDSTONE)
        );

        //noinspection UnstableApiUsage
        MAP = Streams.zip(
            IntStream.range(0, pairs.size()).boxed(),
            pairs.stream(),
            Pair::new
        )
            .map(pair -> new Pair<>(pair.getSecond().getFirst(), new SpecialItem(pair.getFirst(), pair.getSecond().getSecond())))
            .collect(Pair.toMap());
    }
}
