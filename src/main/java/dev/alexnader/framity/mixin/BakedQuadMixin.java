package dev.alexnader.framity.mixin;

import dev.alexnader.framity.adapters.AccessibleBakedQuad;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BakedQuad.class)
public class BakedQuadMixin implements AccessibleBakedQuad {
    @Final
    @Shadow
    protected Sprite sprite;

    @NotNull
    @Override
    public Sprite getSprite() {
        return sprite;
    }
}
