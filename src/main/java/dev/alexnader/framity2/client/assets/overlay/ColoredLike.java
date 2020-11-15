package dev.alexnader.framity2.client.assets.overlay;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ColoredLike {
    public static final Codec<ColoredLike> CODEC = Identifier.CODEC
        .xmap(
            id -> Registry.BLOCK.get(id).getDefaultState(),
            state -> Registry.BLOCK.getId(state.getBlock())
        )
        .xmap(
            ColoredLike::new,
            coloredLike -> coloredLike.colorSource
        );

    private final BlockState colorSource;

    public ColoredLike(final BlockState colorSource) {
        this.colorSource = colorSource;
    }

    public BlockState colorSource() {
        return colorSource;
    }
}
