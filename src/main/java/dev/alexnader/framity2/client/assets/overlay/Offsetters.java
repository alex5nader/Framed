package dev.alexnader.framity2.client.assets.overlay;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import javax.annotation.Nonnull;
import java.util.Optional;

import static dev.alexnader.framity2.Framity2.CODECS;

public class Offsetters {
    public static final Codec<Offsetters> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        CODECS.OFFSETTER.optionalFieldOf("u").forGetter(o -> o.u),
        CODECS.OFFSETTER.optionalFieldOf("v").forGetter(o -> o.v)
    ).apply(inst, Offsetters::new));

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final @Nonnull Optional<Offsetter> u;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public final @Nonnull Optional<Offsetter> v;

    public Offsetters(
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        @Nonnull Optional<Offsetter> u,
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        @Nonnull Optional<Offsetter> v
    ) {
        this.u = u;
        this.v = v;
    }
}
