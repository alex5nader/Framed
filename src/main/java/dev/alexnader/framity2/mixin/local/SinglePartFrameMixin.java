package dev.alexnader.framity2.mixin.local;

import dev.alexnader.framity2.block.FrameSlotInfo;
import dev.alexnader.framity2.block.entity.FrameBlockEntity;
import dev.alexnader.framity2.block.frame.*;
import net.minecraft.block.BlockState;
import net.minecraft.block.TorchBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({
    BlockFrame.class,
    StairsFrame.class,
    FenceFrame.class,
    FenceGateFrame.class,
    TrapdoorFrame.class,
    DoorFrame.class,
    PathFrame.class,
    TorchBlock.class,
    WallTorchBlock.class
})
public abstract class SinglePartFrameMixin implements FrameSlotInfo {
    private SinglePartFrameMixin() {
        throw new IllegalStateException("Mixin constructor should not run.");
    }

    @Override
    public int getRelativeSlotAt(BlockState state, Vec3d posInBlock, Direction side) {
        return 0;
    }

    @Override
    public boolean absoluteSlotIsValid(FrameBlockEntity frame, BlockState state, int slot) {
        return frame.sections().containsSlot(slot);
    }
}
