package dev.alexnader.framity2;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Registrar<T> {
    private final Registry<T> target;

    protected Registrar(final Registry<T> target) {
        this.target = target;
    }

    protected <U extends T> U register(final U value, final Identifier id) {
        return Registry.register(target, id, value);
    }
}
