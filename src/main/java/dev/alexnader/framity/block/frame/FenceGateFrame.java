package dev.alexnader.framity.block.frame;

import dev.alexnader.framity.util.ConstructorCallback;
import net.minecraft.block.FenceGateBlock;

public class FenceGateFrame extends FenceGateBlock implements ConstructorCallback {
    public FenceGateFrame(final Settings settings) {
        super(settings);
        onConstructor();
    }

    @Override
    public void onConstructor() {
        throw new IllegalStateException("FenceGateFrame::onConstructor should be overwritten by mixin.");
    }
}
