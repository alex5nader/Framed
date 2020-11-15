package dev.alexnader.framity2.client.assets.overlay;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

import java.util.EnumMap;
import java.util.Optional;

import static dev.alexnader.framity2.Framity2.CODECS;

public abstract class TextureSource {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static class Entry {
        public final Identifier texture;
        public final Optional<BlockState> materialSource;

        public Entry(final Identifier texture, final Optional<Identifier> materialSource) {
            this.texture = texture;
            this.materialSource = materialSource.map(id -> Registry.BLOCK.get(id).getDefaultState());
        }
    }

    public static final Codec<TextureSource> CODEC = Codec.STRING
        .flatXmap(TextureSourceKind::fromString, kind -> DataResult.success(kind.toString()))
        .dispatch(ts -> ts.kind, kind -> {
            switch (kind) {
            case SINGLE:
                return Single.SINGLE_CODEC;
            case SIDED:
                return Sided.SIDED_CODEC;
            default:
                throw new IllegalStateException("Invalid TextureSourceKind: " + kind);
            }
        });

    public final TextureSourceKind kind;

    protected TextureSource(final TextureSourceKind kind) {
        this.kind = kind;
    }

    public abstract Optional<Entry> entryFor(final Direction side);

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static class Single extends TextureSource {
        @SuppressWarnings("OptionalGetWithoutIsPresent") // guaranteed to be present
        public static final Codec<Single> SINGLE_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Identifier.CODEC.fieldOf("texture").forGetter(s -> s.entry.get().texture),
            Identifier.CODEC.optionalFieldOf("materialSource").forGetter(s -> s.entry.get().materialSource.map(state -> Registry.BLOCK.getId(state.getBlock())))
        ).apply(inst, Single::new));

        // optional used to prevent re-wrapping each request.
        private final Optional<Entry> entry;

        public Single(final Identifier texture, final Optional<Identifier> materialSourceId) {
            super(TextureSourceKind.SINGLE);
            entry = Optional.of(new Entry(texture, materialSourceId));
        }

        @Override
        public Optional<Entry> entryFor(final Direction side) {
            return entry;
        }
    }

    public static class Sided extends TextureSource {
        public static final Codec<Sided> SIDED_CODEC;

        static {
            final Codec<Entry> sidedValueCodec = RecordCodecBuilder.create(inst -> inst.group(
                Identifier.CODEC.fieldOf("texture").forGetter(e -> e.texture),
                Identifier.CODEC.optionalFieldOf("materialFrom").forGetter(e -> e.materialSource.map(state -> Registry.BLOCK.getId(state.getBlock())))
            ).apply(inst, Entry::new));

            SIDED_CODEC = CODECS.sidedMapOf(sidedValueCodec).xmap(
                map -> new Sided(new EnumMap<>(map)),
                sided -> sided.entries
            );
        }

        private final EnumMap<Direction, Entry> entries;

        public Sided(final EnumMap<Direction, Entry> entries) {
            super(TextureSourceKind.SIDED);
            this.entries = entries;
        }

        @Override
        public Optional<Entry> entryFor(final Direction side) {
            return Optional.ofNullable(entries.get(side));
        }
    }
}
