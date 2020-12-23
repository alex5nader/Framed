package dev.alexnader.framity.mixin.mc;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ModelLoader.class)
public class ModelLoaderDebugMixin {
    // synthetic method in upload
    @Inject(
        method = "method_4733",
        at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    void reThrowError(final Identifier id, final CallbackInfo ci, final BakedModel model, final Exception e) {
        throw new RuntimeException(e);
    }
}
