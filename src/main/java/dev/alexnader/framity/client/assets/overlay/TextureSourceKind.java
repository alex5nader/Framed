package dev.alexnader.framity.client.assets.overlay;

import com.mojang.serialization.DataResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum TextureSourceKind {
    SINGLE,
    SIDED;

    public static DataResult<TextureSourceKind> fromString(final String string) {
        switch (string) {
        case "single":
            return DataResult.success(SINGLE);
        case "sided":
            return DataResult.success(SIDED);
        default:
            return DataResult.error("Invalid texture source: " + string);
        }
    }
}
