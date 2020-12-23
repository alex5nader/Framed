package dev.alexnader.framed.block.frame;

import dev.alexnader.framed.util.ConstructorCallback;
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
