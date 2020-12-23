package dev.alexnader.framity.client.transform;

import dev.alexnader.framity.util.Float4;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;

@Environment(EnvType.CLIENT)
public abstract class SpriteApplier {
    public static SpriteApplier ofNullable(@Nullable final Sprite toApply) {
        if (toApply != null) {
            return new Some(toApply);
        } else {
            return NONE;
        }
    }

    public abstract boolean apply(MutableQuadView mqv, Float4 us, Float4 vs);
    public abstract Identifier id();

    public static final SpriteApplier NONE = new SpriteApplier() {
        @Override
        public boolean apply(final MutableQuadView mqv, final Float4 us, final Float4 vs) {
            return false;
        }

        @Override
        public Identifier id() {
            return MissingSprite.getMissingSpriteId();
        }
    };

    public static class Some extends SpriteApplier {
        private final Sprite toApply;

        public Some(final Sprite toApply) {
            this.toApply = toApply;
        }

        @Override
        public boolean apply(final MutableQuadView mqv, final Float4 us, final Float4 vs) {
            mqv.sprite(0, 0, MathHelper.lerp(us.a, toApply.getMinU(), toApply.getMaxU()), MathHelper.lerp(vs.a, toApply.getMinV(), toApply.getMaxV()));
            mqv.sprite(1, 0, MathHelper.lerp(us.b, toApply.getMinU(), toApply.getMaxU()), MathHelper.lerp(vs.b, toApply.getMinV(), toApply.getMaxV()));
            mqv.sprite(2, 0, MathHelper.lerp(us.c, toApply.getMinU(), toApply.getMaxU()), MathHelper.lerp(vs.c, toApply.getMinV(), toApply.getMaxV()));
            mqv.sprite(3, 0, MathHelper.lerp(us.d, toApply.getMinU(), toApply.getMaxU()), MathHelper.lerp(vs.d, toApply.getMinV(), toApply.getMaxV()));
            return true;
        }

        @Override
        public Identifier id() {
            return toApply.getId();
        }
    }
}
