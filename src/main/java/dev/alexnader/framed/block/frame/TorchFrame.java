package dev.alexnader.framed.block.frame;

import dev.alexnader.framed.util.ConstructorCallback;
import net.minecraft.block.TorchBlock;
import net.minecraft.particle.ParticleTypes;

public class TorchFrame extends TorchBlock implements ConstructorCallback {
    public TorchFrame(final Settings settings) {
        super(settings, ParticleTypes.FLAME);
        onConstructor();
    }

    @Override
    public void onConstructor() {
        throw new IllegalStateException("TorchFrame::onConstructor should be overwritten by mixin.");
    }
}
