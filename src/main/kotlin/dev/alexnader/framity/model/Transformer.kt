package dev.alexnader.framity.model

import dev.alexnader.framity.util.SpriteSet
import dev.alexnader.framity.util.then
import net.fabricmc.fabric.api.client.render.ColorProviderRegistry
import net.fabricmc.fabric.api.renderer.v1.Renderer
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.texture.Sprite
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.world.BlockRenderView
import java.util.*
import java.util.function.Supplier

object Tag {
    const val Down = 1
    const val Up = 2
    const val North = 3
    const val South = 4
    const val West = 5
    const val East = 6
}

fun flip(f: Float) = 1 - f

interface MeshTransformer : RenderContext.QuadTransform {
    fun prepare(blockView: BlockRenderView?, state: BlockState?, pos: BlockPos?, randomSupplier: Supplier<Random>?): MeshTransformer
    fun prepare(stack: ItemStack?, randomSupplier: Supplier<Random>?): MeshTransformer
}

class VoxelTransformer : MeshTransformer {
    companion object {
        private val CLIENT: MinecraftClient = MinecraftClient.getInstance()
        private val RENDERER: Renderer = RendererAccess.INSTANCE.renderer
        private val MAT_FINDER: MaterialFinder = RENDERER.materialFinder()

        const val WHITE      = 0x00FFFFFF
        const val FULL_ALPHA = 0xFF000000.toInt()

//        private fun xSprite(qe: MutableQuadView, sprite: Sprite) {
//        }
//
//        private fun ySprite(qe: MutableQuadView, sprite: Sprite) {
//            qe.sprite(0, 0, MathHelper.lerp(qe.x(0), sprite.minU, sprite.maxU), MathHelper.lerp(qe.z(0), sprite.minV, sprite.maxV))
//                .sprite(1, 0, MathHelper.lerp(qe.x(1), sprite.minU, sprite.maxU), MathHelper.lerp(qe.z(1), sprite.minV, sprite.maxV))
//                .sprite(2, 0, MathHelper.lerp(qe.x(2), sprite.minU, sprite.maxU), MathHelper.lerp(qe.z(2), sprite.minV, sprite.maxV))
//                .sprite(3, 0, MathHelper.lerp(qe.x(3), sprite.minU, sprite.maxU), MathHelper.lerp(qe.z(3), sprite.minV, sprite.maxV))
//        }
//
//        private fun zSprite(qe: MutableQuadView, sprite: Sprite) {
//            qe.sprite(0, 0, MathHelper.lerp(qe.x(0), sprite.minU, sprite.maxU), MathHelper.lerp(1 - qe.y(0), sprite.minV, sprite.maxV))
//                .sprite(1, 0, MathHelper.lerp(qe.x(1), sprite.minU, sprite.maxU), MathHelper.lerp(1 - qe.y(1), sprite.minV, sprite.maxV))
//                .sprite(2, 0, MathHelper.lerp(qe.x(2), sprite.minU, sprite.maxU), MathHelper.lerp(1 - qe.y(2), sprite.minV, sprite.maxV))
//                .sprite(3, 0, MathHelper.lerp(qe.x(3), sprite.minU, sprite.maxU), MathHelper.lerp(1 - qe.y(3), sprite.minV, sprite.maxV))
//        }

        private fun applySprite(qe: MutableQuadView, sprite: Sprite, u: (Int) -> Float, v: (Int) -> Float) {
            qe.sprite(0, 0, MathHelper.lerp(u(0), sprite.minU, sprite.maxU), MathHelper.lerp(v(0), sprite.minV, sprite.maxV))
                .sprite(1, 0, MathHelper.lerp(u(1), sprite.minU, sprite.maxU), MathHelper.lerp(v(1), sprite.minV, sprite.maxV))
                .sprite(2, 0, MathHelper.lerp(u(2), sprite.minU, sprite.maxU), MathHelper.lerp(v(2), sprite.minV, sprite.maxV))
                .sprite(3, 0, MathHelper.lerp(u(3), sprite.minU, sprite.maxU), MathHelper.lerp(v(3), sprite.minV, sprite.maxV))
        }
    }

    private val sprites = SpriteSet()

    private var color: Int = 0
    private var mat: RenderMaterial? = null

    override fun prepare(
        blockView: BlockRenderView?,
        state: BlockState?,
        pos: BlockPos?,
        randomSupplier: Supplier<Random>?
    ): MeshTransformer {
        val attachment = (blockView as RenderAttachedBlockView).getBlockEntityRenderAttachment(pos)

        val template = (attachment ?: Blocks.AIR.defaultState) as BlockState
        val block = template.block

        if (block == Blocks.AIR) {
            sprites.clear()
            this.mat = MAT_FINDER.clear().blendMode(0, BlendMode.CUTOUT).find()
        } else {
            this.mat = MAT_FINDER.clear().disableDiffuse(0, false).disableAo(0, false).blendMode(0, BlendMode.fromRenderLayer(RenderLayers.getBlockLayer(template))).find()
            val model = CLIENT.blockRenderManager.getModel(template)
            sprites.prepare(model, randomSupplier?.get())
            val colorProvider = ColorProviderRegistry.BLOCK.get(block)
            if (colorProvider != null) {
                this.color = FULL_ALPHA or colorProvider.getColor(template, blockView, pos, 1)
            }
        }

        return this
    }

    override fun prepare(stack: ItemStack?, randomSupplier: Supplier<Random>?): MeshTransformer {
        this.color = WHITE
        sprites.clear()
        this.mat = MAT_FINDER.clear().find()
        return this
    }

    override fun transform(qe: MutableQuadView?): Boolean {
        qe!!.material(this.mat)

        when (qe.tag()) {
            Tag.Down -> {
                if (sprites.hasColor(Direction.DOWN)) {
                    qe.spriteColor(0, this.color, this.color, this.color, this.color)
                }
                applySprite(qe, sprites[Direction.DOWN], qe::x, qe::z then ::flip)
            }
            Tag.Up -> {
                if (sprites.hasColor(Direction.UP)) {
                    qe.spriteColor(0, this.color, this.color, this.color, this.color)
                }
                applySprite(qe, sprites[Direction.UP], qe::x, qe::z)
            }
            Tag.North -> {
                if (sprites.hasColor(Direction.NORTH)) {
                    qe.spriteColor(0, this.color, this.color, this.color, this.color)
                }
                applySprite(qe, sprites[Direction.NORTH], qe::x, qe::y then ::flip)
            }
            Tag.South -> {
                if (sprites.hasColor(Direction.SOUTH)) {
                    qe.spriteColor(0, this.color, this.color, this.color, this.color)
                }
                applySprite(qe, sprites[Direction.SOUTH], qe::x then ::flip, qe::y then ::flip)
            }
            Tag.East -> {
                if (sprites.hasColor(Direction.EAST)) {
                    qe.spriteColor(0, this.color, this.color, this.color, this.color)
                }
                applySprite(qe, sprites[Direction.EAST], qe::z then ::flip, qe::y then ::flip)
            }
            Tag.West -> {
                if (sprites.hasColor(Direction.WEST)) {
                    qe.spriteColor(0, this.color, this.color, this.color, this.color)
                }
                applySprite(qe, sprites[Direction.WEST], qe::z, qe::y then ::flip)
            }
        }

        return true
    }
}