package dev.alexnader.framed.block.frame;

import dev.alexnader.framed.block.entity.FrameBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.BlockView;

import javax.annotation.Nullable;

import static dev.alexnader.framed.Framed.BLOCK_ENTITY_TYPES;
import static dev.alexnader.framed.Framed.META;

public class WallTorchFrame extends WallTorchBlock implements net.minecraft.block.BlockEntityProvider, Frame {
    public WallTorchFrame(final Settings settings) {
        super(settings, ParticleTypes.FLAME);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(final BlockView world) {
        return new FrameBlockEntity(BLOCK_ENTITY_TYPES.WALL_TORCH_FRAME, base(), META.FRAME_SECTIONS);
    }

    @Override
    public Block base() {
        return Blocks.WALL_TORCH;
    }
}
