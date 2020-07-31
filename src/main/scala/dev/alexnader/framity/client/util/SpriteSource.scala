package dev.alexnader.framity.client.util

import java.{util => ju}

import net.fabricmc.api.{EnvType, Environment}
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.{BakedModel, BakedQuad}
import net.minecraft.client.texture.Sprite
import net.minecraft.util.math.Direction

import scala.jdk.CollectionConverters._

@Environment(EnvType.CLIENT)
class SpriteSource(state: BlockState, model: BakedModel, rand: ju.Random) {
  import dev.alexnader.framity.client.util.MinecraftClientUtil.BakedQuadGetSprite

  private val quads = (0 to 6).view
    .map(ModelHelper.faceFromIndex)
    .map(dir => (dir, model.getQuads(state, dir, rand).asScala))
    .filter(_._2.nonEmpty)
    .toMap

  def apply(dir: Direction, index: Int, color: Int): Option[(Sprite, Option[Int])] = {
    getQuad(dir, index).map(quad => (quad.sprite, Some(color).filter(_ => quad.hasColor)))
  }

  private def getQuad(dir: Direction, index: Int): Option[BakedQuad] =
    this.quads.get(dir).flatMap(quadList => if (quadList.isDefinedAt(index)) Some(quadList(index)) else None)

  def getCount(direction: Direction): Option[Int] = quads.get(direction).map(_.size)
}
