package dev.alexnader.framity.model

import dev.alexnader.framity.util.SpriteSet
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.fabricmc.fabric.api.renderer.v1.Renderer
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.world.BlockRenderView
import java.util.*
import java.util.function.Supplier
import arrow.syntax.function.andThen
import dev.alexnader.framity.adapters.tag
import dev.alexnader.framity.block_entities.FrameEntity

/**
 * Flips a number in the range [0, 1] as if it were in the range [1, 0].
 */
fun flip(f: Float) = 1 - f

/**
 * [RenderContext.QuadTransform] sub-interface which gives the ability
 * to prepare the transformer for block and item contexts.
 */
interface MeshTransformer : RenderContext.QuadTransform {
    fun prepare(blockView: BlockRenderView?, state: BlockState?, pos: BlockPos?, randomSupplier: Supplier<Random>?): MeshTransformer
    fun prepare(stack: ItemStack?, randomSupplier: Supplier<Random>?): MeshTransformer
}

/**
 * [MeshTransformer] implementor which copies the sprite data from a
 * [RenderAttachedBlockView] which returns
 * [RenderAttachmentData][dev.alexnader.framity.block_entities.FrameEntity.RenderAttachmentData]
 * and applies them to the emitter being transformed.
 */
class FrameMeshTransformer(defaultSprite: Sprite) : MeshTransformer {
    companion object {
        private val CLIENT: MinecraftClient = MinecraftClient.getInstance()
        private val RENDERER: Renderer = RendererAccess.INSTANCE.renderer
        private val MAT_FINDER: MaterialFinder = RENDERER.materialFinder()

        const val WHITE      = 0x00FFFFFF
        const val FULL_ALPHA = 0xFF000000.toInt()

        /**
         * Clamps a number to the range [0, 1].
         */
        private fun clamp01(f: Float) = MathHelper.clamp(f, 0f, 1f)

        /**
         * Applies [sprite] to [qe].
         *
         * @param u Function from sprite index to u coordinate.
         * @param v Function from sprite index to v coordinate.
         */
        private fun applySprite(qe: MutableQuadView, sprite: Sprite, u: (Int) -> Float, v: (Int) -> Float) {
            qe.sprite(0, 0, MathHelper.lerp(clamp01(u(0)), sprite.minU, sprite.maxU), MathHelper.lerp(clamp01(v(0)), sprite.minV, sprite.maxV))
                .sprite(1, 0, MathHelper.lerp(clamp01(u(1)), sprite.minU, sprite.maxU), MathHelper.lerp(clamp01(v(1)), sprite.minV, sprite.maxV))
                .sprite(2, 0, MathHelper.lerp(clamp01(u(2)), sprite.minU, sprite.maxU), MathHelper.lerp(clamp01(v(2)), sprite.minV, sprite.maxV))
                .sprite(3, 0, MathHelper.lerp(clamp01(u(3)), sprite.minU, sprite.maxU), MathHelper.lerp(clamp01(v(3)), sprite.minV, sprite.maxV))
        }

        /**
         * Lazily creates new [FrameMeshTransformer] using [spriteId]'s sprite.
         */
        fun ofSprite(spriteId: SpriteIdentifier): () -> MeshTransformer = {
            FrameMeshTransformer(spriteId.sprite)
        }
    }

    private val sprites = SpriteSet(defaultSprite)

    private var color: Int = 0
    private var mat: RenderMaterial? = null

    override fun prepare(
        blockView: BlockRenderView?,
        state: BlockState?,
        pos: BlockPos?,
        randomSupplier: Supplier<Random>?
    ): MeshTransformer {
        val attachment = (blockView as RenderAttachedBlockView).getBlockEntityRenderAttachment(pos) as FrameEntity.RenderAttachmentData?

        if (attachment == null) {
            sprites.clear()
            this.mat = MAT_FINDER.clear().blendMode(0, BlendMode.CUTOUT).find()
        } else {
            val template = attachment.containedState

            this.mat = MAT_FINDER.clear().disableDiffuse(0, false).disableAo(0, false).blendMode(0, BlendMode.fromRenderLayer(RenderLayers.getBlockLayer(template))).find()
            val model = CLIENT.blockRenderManager.getModel(template)
            sprites.prepare(model, randomSupplier?.get())
            val colorProvider = ColorProviderRegistry.BLOCK.get(template.block)
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
            Direction.DOWN.tag -> {
                if (sprites.hasColor(Direction.DOWN)) {
                    qe.spriteColor(0, this.color, this.color, this.color, this.color)
                }
                applySprite(qe, sprites[Direction.DOWN], qe::x, qe::z andThen ::flip)
            }
            Direction.UP.tag -> {
                if (sprites.hasColor(Direction.UP)) {
                    qe.spriteColor(0, this.color, this.color, this.color, this.color)
                }
                applySprite(qe, sprites[Direction.UP], qe::x, qe::z)
            }
            Direction.NORTH.tag -> {
                if (sprites.hasColor(Direction.NORTH)) {
                    qe.spriteColor(0, this.color, this.color, this.color, this.color)
                }
                applySprite(qe, sprites[Direction.NORTH], qe::x, qe::y andThen ::flip)
            }
            Direction.SOUTH.tag -> {
                if (sprites.hasColor(Direction.SOUTH)) {
                    qe.spriteColor(0, this.color, this.color, this.color, this.color)
                }
                applySprite(qe, sprites[Direction.SOUTH], qe::x andThen ::flip, qe::y andThen ::flip)
            }
            Direction.EAST.tag -> {
                if (sprites.hasColor(Direction.EAST)) {
                    qe.spriteColor(0, this.color, this.color, this.color, this.color)
                }
                applySprite(qe, sprites[Direction.EAST], qe::z andThen ::flip, qe::y andThen ::flip)
            }
            Direction.WEST.tag -> {
                if (sprites.hasColor(Direction.WEST)) {
                    qe.spriteColor(0, this.color, this.color, this.color, this.color)
                }
                applySprite(qe, sprites[Direction.WEST], qe::z, qe::y andThen ::flip)
            }
        }

        return true
    }
}