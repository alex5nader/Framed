package dev.alexnader.framity.mixin;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Mixin for {@link BakedQuad} which provides public access to the quad's sprite.
 */
@Mixin(BakedQuad.class)
public interface AccessibleBakedQuad {
    /**
     * Get this {@link BakedQuad}'s sprite.
     * @return this quad's sprite
     */
    @Accessor("sprite")
    Sprite getSprite();
}
