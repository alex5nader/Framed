package dev.alexnader.framity2.block.frame;

import dev.alexnader.framity2.util.ConstructorCallback;
import net.minecraft.block.StairsBlock;

import static dev.alexnader.framity2.Framity2.BLOCKS;

public class StairsFrame extends StairsBlock implements ConstructorCallback {
    public StairsFrame(Settings settings) {
        super(BLOCKS.BLOCK_FRAME.getDefaultState(), settings);
        onConstructor();
    }

    @Override
    public void onConstructor() {
        throw new IllegalStateException("StairsFrame::onConstructor should be overwritten by mixin.");
    }
}
