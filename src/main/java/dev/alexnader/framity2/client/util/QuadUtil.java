package dev.alexnader.framity2.client.util;

import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.ints.Int2FloatFunction;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;

public class QuadUtil {
    public static float calcCenter(Int2FloatFunction key) {
        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;

        for (int i = 0; i <= 3; i++) {
            float cur = key.get(i);
            if (cur > max) {
                max = cur;
            } else if (cur < min) {
                min = cur;
            }
        }

        return (min + max) / 2;
    }
}
