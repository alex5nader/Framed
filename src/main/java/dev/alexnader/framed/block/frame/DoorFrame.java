package dev.alexnader.framed.block.frame;

import net.minecraft.block.DoorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.BlockView;

import javax.annotation.Nullable;

import static dev.alexnader.framed.Framed.BLOCK_ENTITY_TYPES;

public class DoorFrame extends DoorBlock implements net.minecraft.block.BlockEntityProvider, Frame {
    public DoorFrame(final Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(final BlockView world) {
        return BLOCK_ENTITY_TYPES.DOOR_FRAME.instantiate();
    }
}
