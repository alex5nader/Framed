package dev.alexnader.framity2.block.frame;

import dev.alexnader.framity2.util.ConstructorCallback;
import net.minecraft.block.FenceGateBlock;

public class FenceGateFrame extends FenceGateBlock implements ConstructorCallback {
    public FenceGateFrame(Settings settings) {
        super(settings);
        onConstructor();
    }

    @Override
    public void onConstructor() {
        throw new IllegalStateException("FenceGateFrame::onConstructor should be overwritten by mixin.");
    }
}
