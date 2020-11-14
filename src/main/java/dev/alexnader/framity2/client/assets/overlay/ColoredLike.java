package dev.alexnader.framity2.client.assets.overlay;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;

public class ColoredLike {
    public static final Codec<ColoredLike> CODEC = BlockState.CODEC.xmap(
        ColoredLike::new,
        coloredLike -> coloredLike.colorSource
    );

    private final BlockState colorSource;

    public ColoredLike(BlockState colorSource) {
        this.colorSource = colorSource;
    }

    public BlockState colorSource() {
        return colorSource;
    }
}
