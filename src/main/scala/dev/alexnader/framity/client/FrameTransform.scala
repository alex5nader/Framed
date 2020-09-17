package dev.alexnader.framity.client

import java.util.Random
import java.util.function.Supplier
import java.{util => ju}

import dev.alexnader.framity.block.frame.{FrameAccess, FrameBlock}
import dev.alexnader.framity.client.assets.overlay.{IdentifierOverlayAccess, OverlayInfo, TextureSource}
import dev.alexnader.framity.util.ScalaExtensions.{ToTuple, Tuple4Ext}
import grondag.jmx.api.QuadTransformRegistry.QuadTransformSource
import net.fabricmc.api.{EnvType, Environment}
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext.QuadTransform
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.{Sprite, SpriteAtlasTexture}
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.{BlockPos, Direction, MathHelper, Vec3d}
import net.minecraft.world.BlockRenderView
import grondag.frex.api.{Renderer => FrexRenderer}
import grondag.frex.api.material.{RenderMaterial => FrexRenderMaterial}

import scala.collection.View

@Environment(EnvType.CLIENT)
object FrameTransform {
  import dev.alexnader.framity.client.util.SpriteSource

  private def getUvs(mqv: MutableQuadView, direction: Direction): ((Float, Float, Float, Float), (Float, Float, Float, Float)) = direction match {
    case Direction.DOWN => (
      (0 to 3).map(i => MathHelper.clamp(mqv.x(i), 0f, 1f)).toTuple4,
      (0 to 3).map(i => 1 - MathHelper.clamp(mqv.z(i), 0f, 1f)).toTuple4
    )
    case Direction.UP => (
      (0 to 3).map(i => MathHelper.clamp(mqv.x(i), 0f, 1f)).toTuple4,
      (0 to 3).map(i => MathHelper.clamp(mqv.z(i), 0f, 1f)).toTuple4
    )
    case Direction.NORTH => (
      (0 to 3).map(i => 1 - MathHelper.clamp(mqv.x(i), 0f, 1f)).toTuple4,
      (0 to 3).map(i => 1 - MathHelper.clamp(mqv.y(i), 0f, 1f)).toTuple4
    )
    case Direction.SOUTH => (
      (0 to 3).map(i => MathHelper.clamp(mqv.x(i), 0f, 1f)).toTuple4,
      (0 to 3).map(i => 1 - MathHelper.clamp(mqv.y(i), 0f, 1f)).toTuple4
    )
    case Direction.EAST => (
      (0 to 3).map(i => 1 - MathHelper.clamp(mqv.z(i), 0f, 1f)).toTuple4,
      (0 to 3).map(i => 1 - MathHelper.clamp(mqv.y(i), 0f, 1f)).toTuple4
    )
    case Direction.WEST => (
      (0 to 3).map(i => MathHelper.clamp(mqv.z(i), 0f, 1f)).toTuple4,
      (0 to 3).map(i => 1 - MathHelper.clamp(mqv.y(i), 0f, 1f)).toTuple4
    )
  }

  private def getSprite(direction: Direction, source: TextureSource): Option[Sprite] = source match {
    case TextureSource.Sided(map) => map.get(direction).flatMap(id => Option(MinecraftClient.getInstance.getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEX).apply(id)))
    case TextureSource.Single(id) => Option(MinecraftClient.getInstance.getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEX).apply(id))
  }

  private def applySpriteAndColor(mqv: MutableQuadView, maybeSpriteAndColor: Option[(Sprite, Option[Int])], us: (Float, Float, Float, Float), vs: (Float, Float, Float, Float), spriteIndex: Int): Boolean = {
    maybeSpriteAndColor.map { case (sprite, maybeColor) =>
      mqv.sprite(0, spriteIndex, MathHelper.lerp(us._1, sprite.getMinU, sprite.getMaxU), MathHelper.lerp(vs._1, sprite.getMinV, sprite.getMaxV))
      mqv.sprite(1, spriteIndex, MathHelper.lerp(us._2, sprite.getMinU, sprite.getMaxU), MathHelper.lerp(vs._2, sprite.getMinV, sprite.getMaxV))
      mqv.sprite(2, spriteIndex, MathHelper.lerp(us._3, sprite.getMinU, sprite.getMaxU), MathHelper.lerp(vs._3, sprite.getMinV, sprite.getMaxV))
      mqv.sprite(3, spriteIndex, MathHelper.lerp(us._4, sprite.getMinU, sprite.getMaxU), MathHelper.lerp(vs._4, sprite.getMinV, sprite.getMaxV))

      maybeColor.tapEach { color =>
        mqv.spriteColor(spriteIndex, color, color, color, color)
      }
    }.isDefined
  }

  protected case class Data(maybeSprites: Option[SpriteSource], maybeOverlay: Option[OverlayInfo], maybeCachedOverlayColor: Option[Int], color: Int)

  object NonFrex {
    object Source extends QuadTransformSource {
      override def getForBlock(blockRenderView: BlockRenderView, blockState: BlockState, blockPos: BlockPos, supplier: Supplier[Random]): QuadTransform =
        new NonFrex(blockRenderView, blockState, blockPos, supplier)

      override def getForItem(itemStack: ItemStack, supplier: Supplier[Random]): QuadTransform = null
    }
  }

  class NonFrex(blockView: BlockRenderView, state: BlockState, pos: BlockPos, randomSupplier: Supplier[ju.Random]) extends FrameTransform(blockView, state, pos, randomSupplier) {
    private val transformedIndex: ju.EnumMap[Direction, Int] = new ju.EnumMap(classOf[Direction])

    override def transform(mqv: MutableQuadView): Boolean = {
      if (mqv.tag() == 1) {
        return true
      }

      val direction = mqv.lightFace()

      val quadIndex = transformedIndex.getOrDefault(direction, 0)
      transformedIndex.put(direction, quadIndex + 1)

      val partIndex = getPartIndex(mqv, direction)

      val Data(maybeSprites, maybeOverlay, maybeCachedOverlayColor, color) = transformData(partIndex)

      val (maybeSpriteAndColor, us, vs) = {
        val (origUs, origVs) = getUvs(mqv, direction)

        def findMaybeSpriteAndColor(sprites: SpriteSource): Option[(Sprite, Option[Int])] = {
          def cyclicQuadIndex(sprites: SpriteSource): Int = quadIndex % sprites.getCount(direction).getOrElse(1)

          sprites(direction, cyclicQuadIndex(sprites), color)
        }

        if (quadIndex % 2 == 0) { // base quads are first, overlay quads are second
          maybeSprites match {
            case None => return true // don't transform if this quad is base and there's nothing to apply
            case Some(sprites) =>
              (findMaybeSpriteAndColor(sprites), origUs, origVs)
          }
        } else {
          val maybeSpriteAndColor = maybeOverlay match {
            case None => maybeSprites.flatMap(findMaybeSpriteAndColor) // if no overlay, check if base model has more than 1 quad per face and use that
            case Some(overlay) =>
              val maybeSprite = overlay.textureSource.map(textureSource => getSprite(direction, textureSource)) match {
                case Some(None) => return false // there is a texture to use but it doesn't have anything on this direction
                case Some(Some(sprite)) => Some(sprite)
                case None => None
              }

              maybeSprite.map(sprite => (sprite, maybeCachedOverlayColor))
          }

          val (us, vs) = maybeOverlay
            .flatMap(_.getUvs(direction, origUs, origVs))
            .getOrElse(origUs, origVs)

          (maybeSpriteAndColor, us, vs)
        }
      }

      applySpriteAndColor(mqv, maybeSpriteAndColor, us, vs, 0)

      maybeSpriteAndColor.isDefined
    }
  }

  object Frex {
    object Source extends QuadTransformSource {
      override def getForBlock(blockRenderView: BlockRenderView, blockState: BlockState, blockPos: BlockPos, supplier: Supplier[Random]): QuadTransform =
        new Frex(blockRenderView, blockState, blockPos, supplier)

      override def getForItem(itemStack: ItemStack, supplier: Supplier[Random]): QuadTransform = null
    }
  }

  class Frex(blockView: BlockRenderView, state: BlockState, pos: BlockPos, randomSupplier: Supplier[ju.Random]) extends FrameTransform(blockView, state, pos, randomSupplier) {
    override def transform(mqv: MutableQuadView): Boolean = {
      val direction = mqv.lightFace
      val partIndex = getPartIndex(mqv, direction)
      val Data(maybeSprites, maybeOverlay, maybeCachedOverlayColor, color) = transformData(partIndex)
      val (origUs, origVs) = getUvs(mqv, direction)

      //#region Base
      maybeSprites.tapEach { sprites =>
        applySpriteAndColor(mqv, sprites(direction, 0, color), origUs, origVs, 0)
      }
      //#endregion

      //#region Overlay
      val (maybeOverlaySpriteAndColor, us, vs) = maybeOverlay match {
        case Some(overlay) =>
          val maybeOverlaySpriteAndColor = {
            val maybeSprite = overlay.textureSource
              .flatMap { getSprite(direction, _) }

            if (maybeSprite.isEmpty) {
              mqv.material(RendererAccess.INSTANCE.getRenderer.asInstanceOf[FrexRenderer].materialFinder.copyFrom(mqv.material().asInstanceOf[FrexRenderMaterial]).spriteDepth(1).find())
            }

            maybeSprite.map((_, maybeCachedOverlayColor))
          }

          val (us, vs) = overlay.getUvs(direction, origUs, origVs).getOrElse((origUs, origVs))

          (maybeOverlaySpriteAndColor, us, vs)
        case None =>
          val maybeOverlaySpriteAndColor = maybeSprites
            .flatMap { _(direction, 1, color) }

          if (maybeOverlaySpriteAndColor.isEmpty) {
            mqv.material(RendererAccess.INSTANCE.getRenderer.asInstanceOf[FrexRenderer].materialFinder.copyFrom(mqv.material().asInstanceOf[FrexRenderMaterial]).spriteDepth(1).find())
          }

          (maybeOverlaySpriteAndColor, origUs, origVs)
      }
      applySpriteAndColor(mqv, maybeOverlaySpriteAndColor, us, vs, 1)
      //#endregion

      true
    }
  }
}

sealed abstract class FrameTransform(blockView: BlockRenderView, state: BlockState, pos: BlockPos, randomSupplier: Supplier[ju.Random]) extends QuadTransform {
  import dev.alexnader.framity.client.util.SpriteSource

  if (!state.getBlock.isInstanceOf[FrameBlock]) {
    throw new IllegalArgumentException(s"Cannot apply frame transform to non-frame block ${state.getBlock} (at $pos).")
  }

  protected val transformData: Array[FrameTransform.Data] = {
    val attachment = {
      val attachment = blockView.asInstanceOf[RenderAttachedBlockView].getBlockEntityRenderAttachment(pos)
      attachment.asInstanceOf[View[(Option[BlockState], Option[Identifier])]]
    }

    attachment.map { case (baseState, overlayId) =>
      val (color, sprites) = baseState match {
        case Some(baseState) => (
          Option(ColorProviderRegistry.BLOCK.get(baseState.getBlock)).map(_.getColor(baseState, blockView, pos, 1) | 0xFF000000).getOrElse(0xFFFFFFFF),
          Some(new SpriteSource(baseState, MinecraftClient.getInstance.getBlockRenderManager.getModel(baseState), randomSupplier.get)),
        )
        case None => (0xFFFFFFFF, None)
      }
      val overlay = overlayId.flatMap(_.getOverlay)
      val cachedOverlayColor: Option[Int] = overlay
        .flatMap(_.coloredLike)
        .flatMap(coloredLike =>
          Option(ColorProviderRegistry.BLOCK.get(coloredLike.colorSource.getBlock))
            .map(_.getColor(coloredLike.colorSource, blockView, pos, 1))
            .map(_ | 0xFF000000)
        )
      FrameTransform.Data(sprites, overlay, cachedOverlayColor, color)
    }.toArray
  }

  protected def getPartIndex(mqv: MutableQuadView, direction: Direction): Int = {
    state.getBlock.asInstanceOf[FrameAccess].getRelativeSlotAt(
      state,
      new Vec3d(
        (0 to 3).map(mqv.x).toTuple4.center,
        (0 to 3).map(mqv.y).toTuple4.center,
        (0 to 3).map(mqv.z).toTuple4.center
      ),
      direction
    )
  }
}
