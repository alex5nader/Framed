package dev.alexnader.framed.block.frame;

import dev.alexnader.framed.block.entity.FrameBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.BlockView;

import javax.annotation.Nullable;

import static dev.alexnader.framed.Framed.BLOCK_ENTITY_TYPES;
import static dev.alexnader.framed.Framed.META;

public class StairsFrame extends StairsBlock implements net.minecraft.block.BlockEntityProvider, Frame {
    public StairsFrame(final BlockState baseBlockState, final Settings settings) {
        super(baseBlockState, settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(final BlockView world) {
        return BLOCK_ENTITY_TYPES.STAIRS_FRAME.instantiate();
    }
}
