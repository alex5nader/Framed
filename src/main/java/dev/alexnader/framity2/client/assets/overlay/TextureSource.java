package dev.alexnader.framity2.client.assets.overlay;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alexnader.framity2.client.transform.MaterialApplier;
import dev.alexnader.framity2.client.transform.SpriteApplier;
import dev.alexnader.framity2.client.util.ToOptional;
import dev.alexnader.framity2.util.Float4;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static dev.alexnader.framity2.client.Framity2Client.CODECS;

@Environment(EnvType.CLIENT)
public abstract class TextureSource implements ToOptional<TextureSource> {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static class Entry {
        public final @Nonnull SpriteApplier textureApplier;
        public final @Nonnull MaterialApplier materialApplier;

        public static final Entry NONE = new Entry(SpriteApplier.NONE, MaterialApplier.NONE);

        private Entry(@Nonnull final SpriteApplier textureApplier, @Nonnull final MaterialApplier materialApplier) {
            this.textureApplier = textureApplier;
            this.materialApplier = materialApplier;
        }

        public Entry(final Identifier texture, final Optional<Identifier> materialSource) {
            //noinspection deprecation
            final Sprite sprite = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(texture);
            this.textureApplier = SpriteApplier.ofNullable(sprite);

            final Optional<BlockState> materialSourceState = materialSource.map(id -> Registry.BLOCK.get(id).getDefaultState());

            this.materialApplier = MaterialApplier.ofSpriteAndBlockState(sprite, materialSourceState.orElse(null));
        }
    }

    public static final Codec<TextureSource> CODEC = Codec.STRING
        .flatXmap(TextureSourceKind::fromString, kind -> DataResult.success(kind.toString()))
        .dispatch(ts -> ts.kind, kind -> {
            switch (kind) {
            case SINGLE:
                return RecordCodecBuilder.create(inst -> inst.group(
                    Single.SINGLE_CODEC.fieldOf("value").forGetter(i -> (Single) i)
                ).apply(inst, i -> i));
            case SIDED:
                return RecordCodecBuilder.create(inst -> inst.group(
                    Sided.SIDED_CODEC.fieldOf("value").forGetter(i -> (Sided) i)
                ).apply(inst, i -> i));
            default:
                throw new IllegalStateException("Invalid TextureSourceKind: " + kind);
            }
        });

    public final TextureSourceKind kind;

    protected TextureSource(final TextureSourceKind kind) {
        this.kind = kind;
    }

    public boolean apply(final MutableQuadView mqv, final Float4 us, final Float4 vs, final Direction side) {
        final Entry entry = entryFor(side);

        entry.materialApplier.apply(mqv);
        return entry.textureApplier.apply(mqv, us, vs);
    }

    protected abstract @Nonnull Entry entryFor(final Direction side);

    public static class Single extends TextureSource implements ToOptional.Some<TextureSource> {
        public static final Codec<Single> SINGLE_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Identifier.CODEC.fieldOf("texture").forGetter(s -> s.entry.textureApplier.id()),
            Identifier.CODEC.optionalFieldOf("materialSource").forGetter(s -> s.entry.materialApplier.id())
        ).apply(inst, Single::new));

        // optional used to prevent re-wrapping each request.
        private final Entry entry;

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType") // DFU requires Optional params
        public Single(final Identifier texture, final Optional<Identifier> materialSourceId) {
            super(TextureSourceKind.SINGLE);
            entry = new Entry(texture, materialSourceId);
        }

        @Nonnull
        @Override
        protected Entry entryFor(final Direction side) {
            return entry;
        }
    }

    public static class Sided extends TextureSource implements ToOptional.Some<TextureSource> {
        public static final Codec<Sided> SIDED_CODEC;

        static {
            final Codec<Entry> sidedValueCodec = RecordCodecBuilder.create(inst -> inst.group(
                Identifier.CODEC.fieldOf("texture").forGetter(e -> e.textureApplier.id()),
                Identifier.CODEC.optionalFieldOf("materialFrom").forGetter(e -> e.materialApplier.id())
            ).apply(inst, Entry::new));

            SIDED_CODEC = CODECS.sidedMapOf(sidedValueCodec).xmap(
                map -> new Sided(new EnumMap<>(map)),
                sided -> sided.entries
            );
        }

        private final EnumMap<Direction, Entry> entries;

        public Sided(final EnumMap<Direction, Entry> entries) {
            super(TextureSourceKind.SIDED);
            for (final Direction dir : Direction.values()) {
                entries.putIfAbsent(dir, Entry.NONE);
            }
            this.entries = entries;
        }

        @Override
        public Optional<TextureSource> toOptional() {
            return Optional.of(this);
        }

        @Nonnull
        @Override
        protected Entry entryFor(final Direction side) {
            return entries.get(side);
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static TextureSource NONE = new TextureSource(null) {
        @Override
        public Optional<TextureSource> toOptional() {
            return Optional.empty();
        }

        @Override
        public <T> T match(final Function<TextureSource, T> some, final Supplier<T> none) {
            return none.get();
        }

        @Override
        public boolean apply(final MutableQuadView mqv, final Float4 us, final Float4 vs, final Direction side) {
            return false;
        }

        @Nonnull
        @Override
        protected Entry entryFor(final Direction side) {
            //noinspection ReturnOfNull
            return null; // OK because apply is overridden => entryFor is never called
        }
    };
}
