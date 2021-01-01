package dev.alexnader.framed.block.frame;

import dev.alexnader.framed.util.ConstructorCallback;
import net.minecraft.block.WallBlock;

public class WallFrame extends WallBlock implements ConstructorCallback {
    public WallFrame(Settings settings) {
        super(settings);
        onConstructor();
    }

    @Override
    public void onConstructor() {
        throw new IllegalStateException(this.getClass().getSimpleName() + "::onConstructor should be overwritten by mixin.");
    }
}
