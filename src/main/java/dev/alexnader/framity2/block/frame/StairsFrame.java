package dev.alexnader.framity2.block.frame;

import dev.alexnader.framity2.util.ConstructorCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;

public class StairsFrame extends StairsBlock implements ConstructorCallback {
    public StairsFrame(final BlockState baseBlockState, final Settings settings) {
        super(baseBlockState, settings);
        onConstructor();
    }

    @Override
    public void onConstructor() {
        throw new IllegalStateException("StairsFrame::onConstructor should be overwritten by mixin.");
    }
}
