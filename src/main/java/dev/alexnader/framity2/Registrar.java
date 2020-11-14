package dev.alexnader.framity2;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Registrar<T> {
    private final Registry<T> target;

    protected Registrar(Registry<T> target) {
        this.target = target;
    }

    protected <U extends T> U register(U value, Identifier id) {
        return Registry.register(target, id, value);
    }
}
