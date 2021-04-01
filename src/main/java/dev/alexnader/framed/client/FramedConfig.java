package dev.alexnader.framed.client;

import dev.inkwell.conrad.api.Config;
import dev.inkwell.conrad.api.value.ValueKey;
import dev.inkwell.conrad.api.value.data.SaveType;
import dev.inkwell.conrad.api.value.serialization.ConfigSerializer;
import dev.inkwell.conrad.api.value.serialization.FlatOwenSerializer;
import dev.inkwell.owen.OwenElement;
import org.jetbrains.annotations.NotNull;

public class FramedConfig extends Config<OwenElement> {
    public static final ValueKey<Boolean> SHOW_PLACEMENT_PREVIEW = builder(true)
        .with((oldValue, newValue) -> FramePreviewOutline.enabled = newValue)
        .build();

    @Override
    public @NotNull ConfigSerializer<OwenElement> getSerializer() {
        return FlatOwenSerializer.INSTANCE;
    }

    @Override
    public @NotNull SaveType getSaveType() {
        return SaveType.ROOT;
    }
}
