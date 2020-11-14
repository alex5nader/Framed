package dev.alexnader.framity2.client.assets.overlay;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;
import java.util.Map;

import static dev.alexnader.framity2.Framity2.CODECS;
import static dev.alexnader.framity2.client.Framity2Client.CLIENT_OVERLAYS;

public class Overlay {
    public static final Codec<Identifier> PARENT_CODEC = RecordCodecBuilder.create(inst -> inst.group(
        Identifier.CODEC.fieldOf("parent").forGetter(i -> i)
    ).apply(inst, i -> i));

    public static final Codec<Overlay> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        Identifier.CODEC.fieldOf("parent").forGetter(o -> o.parent),
        TextureSource.CODEC.fieldOf("textureSource").forGetter(o -> o.textureSource),
        ColoredLike.CODEC.fieldOf("coloredLike").forGetter(o -> o.coloredLike),
        CODECS.sidedMapOf(Offsetters.CODEC).fieldOf("offsets").forGetter(o -> o.sidedOffsetters)
    ).apply(inst, Overlay::new));

    private final @Nullable Identifier parent;
    private final @Nullable TextureSource textureSource;
    private final @Nullable ColoredLike coloredLike;
    private final @Nullable Map<Direction, Offsetters> sidedOffsetters;

    public Overlay(@Nullable Identifier parent, @Nullable TextureSource textureSource, @Nullable ColoredLike coloredLike, @Nullable Map<Direction, Offsetters> sidedOffsetters) {
        this.parent = parent;
        this.textureSource = textureSource;
        this.coloredLike = coloredLike;
        this.sidedOffsetters = sidedOffsetters;
    }

    @Nullable
    public Identifier parent() {
        return parent;
    }

    public boolean isValid() {
        return textureSource() != null;
    }

    @Nullable
    public TextureSource textureSource() {
        if (textureSource != null) {
            return textureSource;
        } else if (parent != null) {
            return CLIENT_OVERLAYS.getOverlayFor(parent).textureSource();
        } else {
            return null;
        }
    }

    @Nullable
    public ColoredLike coloredLike() {
        if (coloredLike != null) {
            return coloredLike;
        } else if (parent != null) {
            return CLIENT_OVERLAYS.getOverlayFor(parent).coloredLike();
        } else {
            return null;
        }
    }

    @Nullable
    public Map<Direction, Offsetters> sidedOffsetters() {
        if (sidedOffsetters != null) {
            return sidedOffsetters;
        } else if (parent != null) {
            return CLIENT_OVERLAYS.getOverlayFor(parent).sidedOffsetters();
        } else {
            return null;
        }
    }
}
