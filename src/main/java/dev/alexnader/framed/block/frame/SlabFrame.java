package dev.alexnader.framed.block.frame;

import dev.alexnader.framed.block.FrameSlotInfo;
import dev.alexnader.framed.block.entity.FrameBlockEntity;
import dev.alexnader.framed.block.frame.data.Sections;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;

import javax.annotation.Nullable;

import static dev.alexnader.framed.Framed.BLOCK_ENTITY_TYPES;
import static dev.alexnader.framed.Framed.META;

public class SlabFrame extends SlabBlock implements BlockEntityProvider, Frame, FrameSlotInfo {
    public SlabFrame(final Settings settings) {
        super(settings);
    }
    public static final int LOWER_SLOT = 0;
    public static final int UPPER_SLOT = 1;

    @Override
    public Sections sections() {
        return META.SLAB_FRAME_SECTIONS;
    }

    @Override
    public int getRelativeSlotAt(final Vec3d posInBlock, final Direction side) {
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
    public boolean absoluteSlotIsValid(final FrameBlockEntity frame, final BlockState state, final int slot) {
        final int wantedSlot;
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
    public boolean isSideInvisible(final BlockState state, final BlockState stateFrom, final Direction direction) {
        return super.isSideInvisible(state, stateFrom, direction) || (state == stateFrom);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(final BlockView world) {
        return BLOCK_ENTITY_TYPES.SLAB_FRAME.instantiate();
    }
}
