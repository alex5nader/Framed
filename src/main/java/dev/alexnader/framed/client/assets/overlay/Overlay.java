package dev.alexnader.framed.client.assets.overlay;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alexnader.framed.client.assets.Parent;
import dev.alexnader.framed.client.transform.TransformResult;
import dev.alexnader.framed.client.util.ToOptional;
import dev.alexnader.framed.util.Float4;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static dev.alexnader.framed.client.FramedClient.CLIENT_OVERLAYS;

@Environment(EnvType.CLIENT)
public abstract class Overlay implements ToOptional<Overlay> {
    public static Overlay ofNullable(final @Nullable Some some) {
        if (some == null) {
            return NONE;
        } else {
            return some;
        }
    }

    public abstract TransformResult apply(MutableQuadView mqv, Float4 us, Float4 vs, Direction dir);

    public abstract TextureSource textureSource();
    public abstract Optional<ColoredLike> coloredLike();
    public abstract SidedOffsetters.Base sidedOffsetters();

    public static final Overlay NONE = new Overlay() {
        @Override
        public TransformResult apply(final MutableQuadView mqv, final Float4 us, final Float4 vs, final Direction dir) {
            return TransformResult.NOTHING_TO_DO;
        }

        @Override
        public TextureSource textureSource() {
            return TextureSource.NONE;
        }

        @Override
        public Optional<ColoredLike> coloredLike() {
            return Optional.empty();
        }

        @Override
        public SidedOffsetters.Base sidedOffsetters() {
            return SidedOffsetters.NONE;
        }

        @Override
        public Optional<Overlay> toOptional() {
            return Optional.empty();
        }

        @Override
        public <T> T match(final Function<Overlay, T> some, final Supplier<T> none) {
            return none.get();
        }
    };

    public static class Some extends Overlay implements ToOptional.Some<Overlay> {
        public static final Codec<Optional<Identifier>> PARENT_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Identifier.CODEC.optionalFieldOf("parent").forGetter(i -> i)
        ).apply(inst, i -> i));

        public static final Codec<Overlay.Some> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Identifier.CODEC.optionalFieldOf("parent").forGetter(o -> o.parent.id()),
            TextureSource.CODEC.optionalFieldOf("textureSource").forGetter(o -> o.textureSource.toOptional()),
            ColoredLike.CODEC.optionalFieldOf("coloredLike").forGetter(o -> o.coloredLike),
            SidedOffsetters.CODEC.optionalFieldOf("offsets").forGetter(o -> o.sidedOffsetters.toOptional())
        ).apply(inst, Overlay.Some::new));

        private final Parent parent;
        private final TextureSource textureSource;
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private final Optional<ColoredLike> coloredLike;
        private final SidedOffsetters.Base sidedOffsetters;

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType") // DFU requires optional
        public Some(final Optional<Identifier> parent, final Optional<TextureSource> textureSource, final Optional<ColoredLike> coloredLike, final Optional<SidedOffsetters.Base> sidedOffsetters) {
            this.parent = Parent.ofNullable(parent.orElse(null));
            this.textureSource = textureSource.orElse(TextureSource.NONE);
            this.coloredLike = coloredLike;
            this.sidedOffsetters = sidedOffsetters.orElse(SidedOffsetters.NONE);
        }

        @Override
        public TransformResult apply(final MutableQuadView mqv, final Float4 us, final Float4 vs, final Direction dir) {
            final Float4 finalUs = sidedOffsetters().applyUs(us, dir);
            final Float4 finalVs = sidedOffsetters().applyVs(vs, dir);

            // coloredLike is cached on creation and applied outside of Overlay.apply

            return textureSource().apply(mqv, finalUs, finalVs, dir);
        }

        @Override
        public TextureSource textureSource() {
            if (textureSource != TextureSource.NONE) {
                return textureSource;
            } else {
                return parent.run(parent -> CLIENT_OVERLAYS.getOverlayFor(parent).textureSource(), TextureSource.NONE);
            }
        }

        @Override
        public Optional<ColoredLike> coloredLike() {
            if (coloredLike.isPresent()) {
                return coloredLike;
            } else {
                return parent.run(parent -> CLIENT_OVERLAYS.getOverlayFor(parent).coloredLike(), Optional.empty());
            }
        }

        @Override
        public SidedOffsetters.Base sidedOffsetters() {
            if (sidedOffsetters != SidedOffsetters.NONE) {
                return sidedOffsetters;
            } else {
                return parent.run(parent -> CLIENT_OVERLAYS.getOverlayFor(parent).sidedOffsetters(), SidedOffsetters.NONE);
            }
        }
    }
}
