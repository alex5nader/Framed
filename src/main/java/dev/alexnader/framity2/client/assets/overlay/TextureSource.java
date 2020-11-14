package dev.alexnader.framity2.client.assets.overlay;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import java.util.EnumMap;
import java.util.Optional;

import static dev.alexnader.framity2.Framity2.CODECS;

public abstract class TextureSource {
    public static final Codec<TextureSource> CODEC = Codec.STRING
        .flatXmap(TextureSourceKind::fromString, kind -> DataResult.success(kind.toString()))
        .dispatch(ts -> ts.kind, kind -> {
            switch (kind) {
            case SINGLE:
                return Single.SINGLE_CODEC;
            case SIDED:
                return Sided.SIDED_CODEC;
            default:
                throw new IllegalStateException("Unreachable.");
            }
        });

    public final TextureSourceKind kind;

    protected TextureSource(final TextureSourceKind kind) {
        this.kind = kind;
    }

    public abstract Optional<Identifier> textureFor(Direction side);

    public static class Single extends TextureSource {
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        public static final Codec<Single> SINGLE_CODEC = Identifier.CODEC.xmap(Single::new, s -> s.texture.get());

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType") // used to prevent re-wrapping each time texture is requested.
        private final Optional<Identifier> texture;

        public Single(final Identifier texture) {
            super(TextureSourceKind.SINGLE);
            this.texture = Optional.of(texture);
        }

        @Override
        public Optional<Identifier> textureFor(final Direction side) {
            return texture;
        }
    }

    public static class Sided extends TextureSource {
        public static final Codec<Sided> SIDED_CODEC = CODECS.sidedMapOf(Identifier.CODEC).xmap(
            map -> new Sided(new EnumMap<>(map)),
            sided -> sided.textureBySide
        );

        private final EnumMap<Direction, Identifier> textureBySide;

        public Sided(final EnumMap<Direction, Identifier> textureBySide) {
            super(TextureSourceKind.SIDED);
            this.textureBySide = textureBySide;
        }

        @Override
        public Optional<Identifier> textureFor(final Direction side) {
            return Optional.ofNullable(textureBySide.get(side));
        }
    }
}
