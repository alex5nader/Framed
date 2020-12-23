package dev.alexnader.framity.client.assets.overlay;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import javax.annotation.Nonnull;
import java.util.Optional;

import static dev.alexnader.framity.client.FramityClient.CODECS;

@Environment(EnvType.CLIENT)
public class Offsetters {
    public static final Codec<Offsetters> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        CODECS.OFFSETTER.optionalFieldOf("u").forGetter(o -> o.u.toOptional()),
        CODECS.OFFSETTER.optionalFieldOf("v").forGetter(o -> o.v.toOptional())
    ).apply(inst, Offsetters::new));

    public static final Offsetters NONE = new Offsetters();

    public final @Nonnull Offsetter u;
    public final @Nonnull Offsetter v;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public Offsetters(@Nonnull final Optional<Offsetter> u, @Nonnull final Optional<Offsetter> v) {
        this.u = u.orElse(Offsetter.NONE);
        this.v = v.orElse(Offsetter.NONE);
    }

    private Offsetters() {
        this.u = Offsetter.NONE;
        this.v = Offsetter.NONE;
    }
}
