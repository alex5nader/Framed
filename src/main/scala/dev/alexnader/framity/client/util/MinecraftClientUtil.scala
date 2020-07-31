package dev.alexnader.framity.client.util

import net.fabricmc.api.{EnvType, Environment}
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.texture.Sprite

@Environment(EnvType.CLIENT)
object MinecraftClientUtil {
  import dev.alexnader.framity.mixin.AccessibleBakedQuad

  implicit class BakedQuadGetSprite(quad: BakedQuad) {
    def sprite: Sprite = quad.asInstanceOf[AccessibleBakedQuad].getSprite
  }
}
