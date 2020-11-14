package dev.alexnader.framity2.client.assets.overlay;

import dev.alexnader.framity2.util.Float4;
import net.minecraft.util.Identifier;

public interface Offsetter {
    Float4 offset(Float4 original);

    Identifier getId();
}
