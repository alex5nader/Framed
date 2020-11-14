package dev.alexnader.framity2.block.frame;

import dev.alexnader.framity2.util.ConstructorCallback;
import net.minecraft.block.FenceBlock;

public class FenceFrame extends FenceBlock implements ConstructorCallback {
    public FenceFrame(final Settings settings) {
        super(settings);
        onConstructor();
    }

    @Override
    public void onConstructor() {
        throw new IllegalStateException("FenceFrame::onConstructor should be overwritten by mixin.");
    }
}
