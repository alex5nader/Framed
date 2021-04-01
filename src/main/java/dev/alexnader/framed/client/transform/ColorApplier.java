package dev.alexnader.framed.client.transform;

import dev.alexnader.framed.client.util.ToOptional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public abstract class ColorApplier implements ToOptional<ColorApplier> {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static ColorApplier ofOptional(final OptionalInt optional) {
        if (optional.isPresent()) {
            return new Some(optional.getAsInt());
        } else {
            return NONE;
        }
    }

    public abstract void apply(MutableQuadView mqv);

    @SuppressWarnings("java:S1186")
    public static final ColorApplier NONE = new ColorApplier() {
        @Override
        public void apply(final MutableQuadView mqv) { }

        @Override
        public Optional<ColorApplier> toOptional() {
            return Optional.empty();
        }

        @Override
        public <T> T match(final Function<ColorApplier, T> some, final Supplier<T> none) {
            return none.get();
        }
    };

    public static class Some extends ColorApplier implements ToOptional.Some<ColorApplier> {
        private final int color;

        public Some(final int color) {
            this.color = color;
        }

        @Override
        public void apply(final MutableQuadView mqv) {
            mqv.spriteColor(0, color, color, color, color);
        }
    }
}
