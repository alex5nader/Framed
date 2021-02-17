package dev.alexnader.framed.block.frame;

import dev.alexnader.framed.block.PathBlock;
import dev.alexnader.framed.block.entity.FrameBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

import javax.annotation.Nullable;

import static dev.alexnader.framed.Framed.BLOCK_ENTITY_TYPES;
import static dev.alexnader.framed.Framed.META;

public class PathFrame extends PathBlock implements net.minecraft.block.BlockEntityProvider, Frame {
    public PathFrame(final Settings settings) {
        super(settings);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isSideInvisible(final BlockState state, final BlockState stateFrom, final Direction direction) {
        return super.isSideInvisible(state, stateFrom, direction) || (state == stateFrom);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(final BlockView world) {
        return new FrameBlockEntity(BLOCK_ENTITY_TYPES.PATH_FRAME, base(), META.FRAME_SECTIONS);
    }

    @Override
    public Block base() {
        return Blocks.GRASS_PATH;
    }
}
