package dev.alexnader.framity2.client.assets.overlay;

import com.mojang.serialization.DataResult;

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
