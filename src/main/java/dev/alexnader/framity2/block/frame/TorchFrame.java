package dev.alexnader.framity2.block.frame;

import dev.alexnader.framity2.util.ConstructorCallback;
import net.minecraft.block.TorchBlock;
import net.minecraft.particle.ParticleTypes;

public class TorchFrame extends TorchBlock implements ConstructorCallback {
    public TorchFrame(Settings settings) {
        super(settings, ParticleTypes.FLAME);
        onConstructor();
    }

    @Override
    public void onConstructor() {
        throw new IllegalStateException("TorchFrame::onConstructor should be overwritten by mixin.");
    }
}
