package dev.alexnader.framity2.block.frame;

import dev.alexnader.framity2.block.PathBlock;
import dev.alexnader.framity2.util.ConstructorCallback;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;

public class PathFrame extends PathBlock implements ConstructorCallback {
    public PathFrame(final Settings settings) {
        super(settings);
        onConstructor();
    }

    @Override
    public void onConstructor() {
        throw new IllegalStateException("PathFrame::onConstructor should be overwritten by mixin.");
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isSideInvisible(final BlockState state, final BlockState stateFrom, final Direction direction) {
        return super.isSideInvisible(state, stateFrom, direction) || (state == stateFrom);
    }
}
