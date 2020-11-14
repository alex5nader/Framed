package dev.alexnader.framity2.block.frame;

import dev.alexnader.framity2.block.FrameSlotInfo;
import dev.alexnader.framity2.block.entity.FrameBlockEntity;
import dev.alexnader.framity2.block.frame.data.Sections;
import dev.alexnader.framity2.util.ConstructorCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class SlabFrame extends SlabBlock implements FrameSlotInfo, ConstructorCallback {
    public SlabFrame(Settings settings) {
        super(settings);
        onConstructor();
    }

    @Override
    public void onConstructor() {
        throw new IllegalStateException("SlabFrame::onConstructor should be overwritten by mixin.");
    }
    public static int LOWER_SLOT = 0;
    public static int UPPER_SLOT = 1;

    @Override
    public int getRelativeSlotAt(BlockState state, Vec3d posInBlock, Direction side) {
        switch (side) {
        case UP:
            return posInBlock.y == 0.5 ? LOWER_SLOT : UPPER_SLOT;
        case DOWN:
            return posInBlock.y == 0.5 ? UPPER_SLOT : LOWER_SLOT;
        default:
            return posInBlock.y < 0.5 ? LOWER_SLOT : UPPER_SLOT;
        }
    }

    @Override
    public boolean absoluteSlotIsValid(FrameBlockEntity frame, BlockState state, int slot) {
        int wantedSlot;
        switch (state.get(Properties.SLAB_TYPE)) {
        case DOUBLE:
            return true;
        case TOP:
            wantedSlot = UPPER_SLOT;
            break;
        case BOTTOM:
            wantedSlot = LOWER_SLOT;
            break;
        default:
            throw new IllegalStateException("Unreachable.");
        }
        switch (frame.sections().findSectionIndexOf(slot)) {
        case Sections.BASE_INDEX:
            return frame.sections().base().makeAbsolute(slot) == wantedSlot;
        case Sections.OVERLAY_INDEX:
            return frame.sections().overlay().makeAbsolute(slot) == wantedSlot;
        case Sections.SPECIAL_INDEX:
            return true;
        default:
            throw new IllegalArgumentException("Invalid slot for slab frame: " + slot);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        return super.isSideInvisible(state, stateFrom, direction) || (state == stateFrom);
    }
}
