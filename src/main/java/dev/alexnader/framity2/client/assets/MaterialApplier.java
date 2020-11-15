package dev.alexnader.framity2.client.assets;

import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.minecraft.util.Identifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public abstract class MaterialApplier {
    public static MaterialApplier ofNullable(@Nullable final Identifier id, @Nullable final RenderMaterial toApply) {
        if (id != null && toApply != null) {
            return new Some(id, toApply);
        } else {
            return NONE;
        }
    }

    public static final MaterialApplier NONE = new MaterialApplier() {
        @Override
        public void apply(final MutableQuadView mqv) { }

        @Override
        public Optional<Identifier> id() {
            return Optional.empty();
        }
    };

    public abstract void apply(MutableQuadView mqv);
    public abstract Optional<Identifier> id();

    public static class Some extends MaterialApplier {
        private final @Nonnull Identifier id;
        private final @Nonnull RenderMaterial toApply;

        public Some(final @Nonnull Identifier id, final @Nonnull RenderMaterial toApply) {
            this.id = id;
            this.toApply = toApply;
        }

        @Override
        public void apply(final MutableQuadView mqv) {
            mqv.material(toApply);
        }

        @Override
        public Optional<Identifier> id() {
            return Optional.of(id);
        }
    }
}
