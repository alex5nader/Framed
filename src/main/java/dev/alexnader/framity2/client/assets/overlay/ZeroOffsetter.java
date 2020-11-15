package dev.alexnader.framity2.client.assets.overlay;

import dev.alexnader.framity2.client.util.ToOptional;
import dev.alexnader.framity2.util.Float4;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ZeroOffsetter implements Offsetter, ToOptional.Some<Offsetter> {
    private final Identifier id;

    public ZeroOffsetter(final Identifier id) {
        this.id = id;
    }

    @Override
    public Float4 offset(final Float4 original) {
        final float min = original.min();
        final float max = original.max();

        final float delta = max - min;

        if (original.a == min) {
            return Float4.of(0, delta, delta, 0);
        } else {
            return Float4.of(delta, 0, 0, delta);
        }
    }

    @Override
    public Identifier getId() {
        return id;
    }
}
