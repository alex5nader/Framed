package dev.alexnader.framed.block.frame;

import dev.alexnader.framed.util.ConstructorCallback;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.particle.ParticleTypes;

public class WallTorchFrame extends WallTorchBlock implements ConstructorCallback {
    public WallTorchFrame(final Settings settings) {
        super(settings, ParticleTypes.FLAME);
        onConstructor();
    }

    @Override
    public void onConstructor() {
        throw new IllegalStateException("WallTorchFrame::onConstructor should be overwritten by mixin.");
    }
}
