package dev.alexnader.framed.block.frame;

import dev.alexnader.framed.util.ConstructorCallback;
import net.minecraft.block.DoorBlock;

public class DoorFrame extends DoorBlock implements ConstructorCallback {
    public DoorFrame(final Settings settings) {
        super(settings);
        onConstructor();
    }

    @Override
    public void onConstructor() {
        throw new IllegalStateException("DoorFrame::onConstructor should be overwritten by mixin.");
    }
}
