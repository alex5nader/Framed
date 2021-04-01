package dev.alexnader.framed.block.frame;

import net.minecraft.block.WallTorchBlock;
import net.minecraft.particle.ParticleTypes;

public class WallTorchFrame extends WallTorchBlock {
    public WallTorchFrame(final Settings settings) {
        super(settings, ParticleTypes.FLAME);
    }
}
