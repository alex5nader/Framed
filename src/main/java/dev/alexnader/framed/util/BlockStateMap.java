package dev.alexnader.framed.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;

import javax.annotation.Nullable;
import java.util.Set;

public class BlockStateMap<V> {
    private final Set<Property<?>> importantProperties;
    private final Int2ObjectOpenHashMap<V> states = new Int2ObjectOpenHashMap<>();

    private int hash(BlockState state) {
        int hash = 17;
        for (Property<?> property : importantProperties) {
            hash = 31 * hash + state.get(property).hashCode();
        }
        return hash;
    }

    public BlockStateMap(Set<Property<?>> importantProperties) {
        this.importantProperties = importantProperties;
    }

    public void put(BlockState state, V value) {
        states.put(hash(state), value);
    }

    public void put(int hash, V value) {
        states.put(hash, value);
    }

    public @Nullable V get(BlockState state) {
        return states.get(hash(state));
    }

    public @Nullable V get(int hash) {
        return states.get(hash);
    }

    public void clear() {
        states.clear();
    }

    public boolean containsEquivalent(BlockState state) {
        return states.containsKey(hash(state));
    }
}
