package dev.alexnader.framity2.util;

import net.minecraft.util.Identifier;

public class Identifiable<A> {
    private final A value;
    private final Identifier id;

    public Identifiable(A value, Identifier id) {
        this.value = value;
        this.id = id;
    }

    public A value() {
        return value;
    }

    public Identifier id() {
        return id;
    }
}
