package dev.alexnader.framity2.block;

import dev.alexnader.framity2.block.entity.FrameBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public interface FrameSlotInfo {
    int getRelativeSlotAt(BlockState state, Vec3d posInBlock, Direction side);
    boolean absoluteSlotIsValid(FrameBlockEntity frame, BlockState state, int slot);
}
