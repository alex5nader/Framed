package dev.alexnader.framed.client.assets.overlay;

import com.mojang.serialization.Codec;
import dev.alexnader.framed.util.Identifiable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class OffsetterRegistry {
    private OffsetterRegistry() { }

    private static final Map<Identifier, Identifiable<Codec<Offsetter>>> registeredOffsetters = new HashMap<>();

    public static void register(final Identifier id, final Codec<Offsetter> codec) {
        registeredOffsetters.put(id, new Identifiable<>(codec, id));
    }

    public static Identifiable<Codec<Offsetter>> get(final Identifier id) {
        return registeredOffsetters.get(id);
    }
}
