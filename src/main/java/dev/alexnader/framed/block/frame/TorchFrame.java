package dev.alexnader.framed.block.frame;

import dev.alexnader.framed.block.entity.FrameBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.TorchBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.BlockView;

import javax.annotation.Nullable;

import static dev.alexnader.framed.Framed.BLOCK_ENTITY_TYPES;
import static dev.alexnader.framed.Framed.META;

public class TorchFrame extends TorchBlock implements net.minecraft.block.BlockEntityProvider, Frame {
    public TorchFrame(final Settings settings) {
        super(settings, ParticleTypes.FLAME);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(final BlockView world) {
        return BLOCK_ENTITY_TYPES.TORCH_FRAME.instantiate();
    }
}
