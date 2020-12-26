package dev.alexnader.framed.block.frame;

import dev.alexnader.framed.util.ConstructorCallback;
import net.minecraft.block.PressurePlateBlock;

public class PressurePlateFrame extends PressurePlateBlock implements ConstructorCallback {
    public PressurePlateFrame(final Settings settings) {
        super(ActivationRule.MOBS, settings);
        onConstructor();
    }

    @Override
    public void onConstructor() {
        throw new IllegalStateException("BlockFrame::onConstructor should be overwritten by mixin.");
    }
}
