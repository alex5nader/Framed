package dev.alexnader.framed.block.frame;

import dev.alexnader.framed.block.entity.FrameBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarpetBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.BlockView;

import javax.annotation.Nullable;

import static dev.alexnader.framed.Framed.BLOCK_ENTITY_TYPES;
import static dev.alexnader.framed.Framed.META;

public class CarpetFrame extends CarpetBlock implements net.minecraft.block.BlockEntityProvider, Frame {
    public CarpetFrame(Settings settings) {
        super(null, settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(final BlockView world) {
        return new FrameBlockEntity(BLOCK_ENTITY_TYPES.CARPET_FRAME, base(), META.FRAME_SECTIONS);
    }

    @Override
    public Block base() {
        return Blocks.BLACK_CARPET;
    }
}
