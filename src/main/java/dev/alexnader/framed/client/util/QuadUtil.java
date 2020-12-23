package dev.alexnader.framed.client.util;

import it.unimi.dsi.fastutil.ints.Int2FloatFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class QuadUtil {
    public static float calcCenter(final Int2FloatFunction key) {
        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;

        for (int i = 0; i <= 3; i++) {
            final float cur = key.get(i);
            if (cur > max) {
                max = cur;
            } else if (cur < min) {
                min = cur;
            }
        }

        return (min + max) / 2;
    }
}
