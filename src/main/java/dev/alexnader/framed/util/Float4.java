package dev.alexnader.framed.util;

import java.util.PrimitiveIterator;

public class Float4 {
    public final float a;
    public final float b;
    public final float c;
    public final float d;

    public Float4(final float a, final float b, final float c, final float d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public static Float4 of(final float a, final float b, final float c, final float d) {
        return new Float4(a, b, c, d);
    }

    public float min() {
        return Math.min(Math.min(Math.min(a, b), c), d);
    }

    public float max() {
        return Math.max(Math.max(Math.max(a, b), c), d);
    }

    public static Float4 fromIterator(final PrimitiveIterator.OfDouble doubles) {
        return new Float4((float) doubles.nextDouble(), (float) doubles.nextDouble(), (float) doubles.nextDouble(), (float) doubles.nextDouble());
    }
}
