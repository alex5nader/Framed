package dev.alexnader.framity2.client.assets.overlay;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static dev.alexnader.framity2.Framity2.CODECS;
import static dev.alexnader.framity2.client.Framity2Client.CLIENT_OVERLAYS;
import static dev.alexnader.framity2.util.FunctionalUtil.orElseFlatGet;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Overlay {
    public static final Codec<Optional<Identifier>> PARENT_CODEC = RecordCodecBuilder.create(inst -> inst.group(
        Identifier.CODEC.optionalFieldOf("parent").forGetter(i -> i)
    ).apply(inst, i -> i));

    public static final Codec<Overlay> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        Identifier.CODEC.optionalFieldOf("parent").forGetter(o -> o.parent),
        TextureSource.CODEC.optionalFieldOf("textureSource").forGetter(o -> o.textureSource),
        ColoredLike.CODEC.optionalFieldOf("coloredLike").forGetter(o -> o.coloredLike),
        CODECS.sidedMapOf(Offsetters.CODEC).optionalFieldOf("offsets").forGetter(o -> o.sidedOffsetters)
    ).apply(inst, Overlay::new));

    private final Optional<Identifier> parent;
    private final Optional<TextureSource> textureSource;
    private final Optional<ColoredLike> coloredLike;
    private final Optional<Map<Direction, Offsetters>> sidedOffsetters;

    public Overlay(final Optional<Identifier> parent, final Optional<TextureSource> textureSource, final Optional<ColoredLike> coloredLike, final Optional<Map<Direction, Offsetters>> sidedOffsetters) {
        this.parent = parent;
        this.textureSource = textureSource;
        this.coloredLike = coloredLike;
        this.sidedOffsetters = sidedOffsetters;
    }

    private <A> Optional<A> getFromParent(final Function<Overlay, Optional<A>> key) {
        return orElseFlatGet(
            key.apply(this),
            () -> parent.flatMap(parent -> CLIENT_OVERLAYS.getOverlayFor(parent)).flatMap(key)
        );
    }

    public Optional<TextureSource> textureSource() {
        return getFromParent(Overlay::textureSource);
    }

    public Optional<ColoredLike> coloredLike() {
        return getFromParent(Overlay::coloredLike);
    }

    public Optional<Map<Direction, Offsetters>> sidedOffsetters() {
        return getFromParent(Overlay::sidedOffsetters);
    }
}
