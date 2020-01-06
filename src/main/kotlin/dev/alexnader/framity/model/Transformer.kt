package dev.alexnader.framity.model

import dev.alexnader.framity.clamp
import dev.alexnader.framity.util.SpriteSet
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

fun flip(f: Float) = 1 - f

interface MeshTransformer : RenderContext.QuadTransform {

    fun prepare(blockView: BlockRenderView?, state: BlockState?, pos: BlockPos?, randomSupplier: Supplier<Random>?): MeshTransformer
    fun prepare(stack: ItemStack?, randomSupplier: Supplier<Random>?): MeshTransformer
}

class VoxelTransformer(defaultSprite: Sprite) : MeshTransformer {
    companion object {
        private val CLIENT: MinecraftClient = MinecraftClient.getInstance()
        private val RENDERER: Renderer = RendererAccess.INSTANCE.renderer
        private val MAT_FINDER: MaterialFinder = RENDERER.materialFinder()

        const val WHITE      = 0x00FFFFFF
        const val FULL_ALPHA = 0xFF000000.toInt()

        private val CLAMP_01 = clamp(0f)(1f)

        private fun applySprite(qe: MutableQuadView, sprite: Sprite, u: (Int) -> Float, v: (Int) -> Float) {
            val clampedU = u andThen CLAMP_01
            val clampedV = v andThen CLAMP_01
            qe.sprite(0, 0, MathHelper.lerp(clampedU(0), sprite.minU, sprite.maxU), MathHelper.lerp(clampedV(0), sprite.minV, sprite.maxV))
                .sprite(1, 0, MathHelper.lerp(clampedU(1), sprite.minU, sprite.maxU), MathHelper.lerp(clampedV(1), sprite.minV, sprite.maxV))
                .sprite(2, 0, MathHelper.lerp(clampedU(2), sprite.minU, sprite.maxU), MathHelper.lerp(clampedV(2), sprite.minV, sprite.maxV))
                .sprite(3, 0, MathHelper.lerp(clampedU(3), sprite.minU, sprite.maxU), MathHelper.lerp(clampedV(3), sprite.minV, sprite.maxV))
        }

        fun ofSprite(spriteId: SpriteIdentifier): () -> MeshTransformer = {
            VoxelTransformer(spriteId.sprite)
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