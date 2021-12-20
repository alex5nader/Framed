package dev.alexnader.framed.block.frame;

import net.minecraft.block.CarpetBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.BlockView;

import javax.annotation.Nullable;

import static dev.alexnader.framed.Framed.BLOCK_ENTITY_TYPES;

public class CarpetFrame extends CarpetBlock implements net.minecraft.block.BlockEntityProvider, Frame {
    public CarpetFrame(Settings settings) {
        super(null, settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(final BlockView world) {
        return BLOCK_ENTITY_TYPES.CARPET_FRAME.instantiate();
    }
}
