package dev.alexnader.framity2.client.assets.overlay;

import com.mojang.serialization.Codec;
import dev.alexnader.framity2.util.Identifiable;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class OffsetterRegistry {
    private OffsetterRegistry() { }

    private static final Map<Identifier, Identifiable<Codec<Offsetter>>> registeredOffsetters = new HashMap<>();

    public static void register(Identifier id, Codec<Offsetter> codec) {
        registeredOffsetters.put(id, new Identifiable<>(codec, id));
    }

    public static Identifiable<Codec<Offsetter>> get(Identifier id) {
        return registeredOffsetters.get(id);
    }
}
