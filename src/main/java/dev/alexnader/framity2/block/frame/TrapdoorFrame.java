package dev.alexnader.framity2.block.frame;

import dev.alexnader.framity2.util.ConstructorCallback;
import net.minecraft.block.TrapdoorBlock;

public class TrapdoorFrame extends TrapdoorBlock implements ConstructorCallback {
    public TrapdoorFrame(Settings settings) {
        super(settings);
        onConstructor();
    }

    @Override
    public void onConstructor() {
        throw new IllegalStateException("TrapdoorFrame::onConstructor should be overwritten by mixin.");
    }
}
