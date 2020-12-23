package dev.alexnader.framity.block;

import dev.alexnader.framity.util.ConstructorCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class PathBlock extends Block implements ConstructorCallback {
    public PathBlock(final Settings settings) {
        super(settings);
        onConstructor();
    }

    @Override
    public void onConstructor() {
        throw new IllegalStateException("PathBlock::onConstructor should be overwritten by mixin.");
    }
    private static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 15, 16);

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasSidedTransparency(final BlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getOutlineShape(final BlockState state, final BlockView world, final BlockPos pos, final ShapeContext context) {
        return SHAPE;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean canPathfindThrough(final BlockState state, final BlockView world, final BlockPos pos, final NavigationType type) {
        return false;
    }
}
