package dev.alexnader.framity2.util;

import java.util.PrimitiveIterator;

public class Float4 {
    public final float a;
    public final float b;
    public final float c;
    public final float d;

    public Float4(float a, float b, float c, float d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public static Float4 fromIterator(PrimitiveIterator.OfDouble doubles) {
        return new Float4((float) doubles.nextDouble(), (float) doubles.nextDouble(), (float) doubles.nextDouble(), (float) doubles.nextDouble());
    }
}
