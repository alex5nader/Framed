package dev.alexnader.framity.client.assets.overlay;

import com.mojang.serialization.Codec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@Environment(EnvType.CLIENT)
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
