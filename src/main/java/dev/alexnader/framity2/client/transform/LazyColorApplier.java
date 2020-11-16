package dev.alexnader.framity2.client.transform;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;

public abstract class LazyColorApplier {
    public static final LazyColorApplier NONE = new LazyColorApplier() {
        @Override
        public void apply(final MutableQuadView mqv, final int color) { }
    };
    
    public abstract void apply(MutableQuadView mqv, int color);

    public static class Some extends LazyColorApplier {
        @Override
        public void apply(final MutableQuadView mqv, final int color) {
            mqv.spriteColor(0, color, color, color, color);
        }
    }
}
