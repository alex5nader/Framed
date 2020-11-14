package dev.alexnader.framity2.block.frame;

import dev.alexnader.framity2.util.ConstructorCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;

public class BlockFrame extends Block implements ConstructorCallback {
    public BlockFrame(final Settings settings) {
        super(settings);
        onConstructor();
    }

    @Override
    public void onConstructor() {
        throw new IllegalStateException("BlockFrame::onConstructor should be overwritten by mixin.");
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isSideInvisible(final BlockState state, final BlockState stateFrom, final Direction direction) {
        return super.isSideInvisible(state, stateFrom, direction) || (state == stateFrom);
    }
}
