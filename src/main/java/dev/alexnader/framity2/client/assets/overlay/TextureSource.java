package dev.alexnader.framity2.client.assets.overlay;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import java.util.EnumMap;

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

    public TextureSourceKind kind;

    protected TextureSource(TextureSourceKind kind) {
        this.kind = kind;
    }

    public TextureSourceKind kind() {
        return kind;
    }

    public abstract Identifier textureFor(Direction side);

    public static class Single extends TextureSource {
        public static final Codec<Single> SINGLE_CODEC = Identifier.CODEC.xmap(Single::new, s -> s.texture);

        private final Identifier texture;

        public Single(Identifier texture) {
            super(TextureSourceKind.SINGLE);
            this.texture = texture;
        }

        @Override
        public Identifier textureFor(Direction side) {
            return texture;
        }
    }

    public static class Sided extends TextureSource {
        public static final Codec<Sided> SIDED_CODEC = CODECS.sidedMapOf(Identifier.CODEC).xmap(
            map -> new Sided(new EnumMap<>(map)),
            sided -> sided.textureBySide
        );

        private final EnumMap<Direction, Identifier> textureBySide;

        public Sided(EnumMap<Direction, Identifier> textureBySide) {
            super(TextureSourceKind.SIDED);
            this.textureBySide = textureBySide;
        }

        @Override
        public Identifier textureFor(Direction side) {
            return textureBySide.get(side);
        }
    }
}
