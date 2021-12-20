package dev.alexnader.framed.block.frame;

import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.BlockView;

import javax.annotation.Nullable;

import static dev.alexnader.framed.Framed.BLOCK_ENTITY_TYPES;

public class TrapdoorFrame extends TrapdoorBlock implements net.minecraft.block.BlockEntityProvider, Frame {
    public TrapdoorFrame(final Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(final BlockView world) {
        return BLOCK_ENTITY_TYPES.TRAPDOOR_FRAME.instantiate();
    }
}
