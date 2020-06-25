package dev.alexnader.framity.model.v2

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
import dev.alexnader.framity.data.OverlayKind
import dev.alexnader.framity.util.andThen
import net.minecraft.client.texture.SpriteAtlasTexture

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
 * [RenderAttachedBlockView] and applies them to the emitter being transformed.
 */
class FrameMeshTransformer(defaultSprite: Sprite) : MeshTransformer {
    companion object {
        private val CLIENT: MinecraftClient = MinecraftClient.getInstance()
        private val RENDERER: Renderer = RendererAccess.INSTANCE.renderer
        private val MAT_FINDER: MaterialFinder = RENDERER.materialFinder()

        const val WHITE      = 0x00FFFFFF
        const val FULL_ALPHA = 0xFF000000.toInt()

        private fun uvGetters(qe: MutableQuadView): Pair<(Int) -> Float, (Int) -> Float> = when (qe.nominalFace()) {
            Direction.DOWN -> Pair(qe::x, qe::z andThen ::flip)
            Direction.UP -> Pair(qe::x, qe::z)
            Direction.NORTH -> Pair(qe::x, qe::y andThen ::flip)
            Direction.SOUTH -> Pair(qe::x andThen ::flip, qe::y andThen ::flip)
            Direction.EAST -> Pair(qe::z andThen ::flip, qe::y andThen ::flip)
            Direction.WEST -> Pair(qe::z, qe::y andThen ::flip)
            null -> error("oh god oh fuck no nominal face")
        }

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
            qe.sprite(0, 0, MathHelper.lerp(
                clamp01(
                    u(0)
                ), sprite.minU, sprite.maxU), MathHelper.lerp(
                clamp01(
                    v(0)
                ), sprite.minV, sprite.maxV))
                .sprite(1, 0, MathHelper.lerp(
                    clamp01(
                        u(1)
                    ), sprite.minU, sprite.maxU), MathHelper.lerp(
                    clamp01(
                        v(1)
                    ), sprite.minV, sprite.maxV))
                .sprite(2, 0, MathHelper.lerp(
                    clamp01(
                        u(2)
                    ), sprite.minU, sprite.maxU), MathHelper.lerp(
                    clamp01(
                        v(2)
                    ), sprite.minV, sprite.maxV))
                .sprite(3, 0, MathHelper.lerp(
                    clamp01(
                        u(3)
                    ), sprite.minU, sprite.maxU), MathHelper.lerp(
                    clamp01(
                        v(3)
                    ), sprite.minV, sprite.maxV))
        }
    }

    private val sprites = SpriteSet(defaultSprite)
    private val transformed: EnumMap<Direction?, Int> = EnumMap(Direction::class.java)

    private var overlayKind = OverlayKind.None
    private var overlayColor: Int? = null

    private var color = 0
    private var mat: RenderMaterial? = null

    @Suppress("UNCHECKED_CAST")
    override fun prepare(
        blockView: BlockRenderView?,
        state: BlockState?,
        pos: BlockPos?,
        randomSupplier: Supplier<Random>?
    ): MeshTransformer {
        val (containedState, overlayKind) = (blockView as RenderAttachedBlockView).getBlockEntityRenderAttachment(pos) as Pair<BlockState?, OverlayKind>

        if (containedState == null) {
            sprites.clear()
            this.mat = MAT_FINDER.clear().blendMode(0, BlendMode.CUTOUT).find()
        } else {
            this.mat = MAT_FINDER.clear().disableDiffuse(0, false).disableAo(0, false).blendMode(0, BlendMode.fromRenderLayer(RenderLayers.getBlockLayer(containedState))).find()
            val model = CLIENT.blockRenderManager.getModel(containedState)
            sprites.prepare(model, randomSupplier?.get())

            val colorProvider = ColorProviderRegistry.BLOCK.get(containedState.block)
            if (colorProvider != null) {
                this.color = FULL_ALPHA or colorProvider.getColor(containedState, blockView, pos, 1)
            }
        }

        this.overlayKind = overlayKind
        this.overlayColor = ColorProviderRegistry.BLOCK.get(overlayKind.stateForColor?.block)
            ?.getColor(overlayKind.stateForColor, blockView, pos, 1)
            ?.let { FULL_ALPHA or it }

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

        val direction = qe.nominalFace()

        val index = this.transformed[direction] ?: 0
        this.transformed[direction] = index + 1

        val (u, v) = uvGetters(qe)

        val (sprite, color) = if (index == 0) {
            // base quad
            if (sprites.isDefault) {
                return true
            }

            val sprite = this.sprites[direction, index]
            val color = if (this.sprites.hasColor(direction, index)) {
                this.color
            } else {
                null
            }

            Pair(sprite, color)
        } else {
            // overlay quad
            if (this.overlayKind == OverlayKind.None) {
                return false
            }

            val sprite = when (direction) {
                Direction.UP -> {
                    println("Getting top overlay texture")
                    @Suppress("deprecation")
                    CLIENT.getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEX).apply(this.overlayKind.sprites?.top)
                }
                Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST -> {
                    println("Getting side overlay texture")
                    @Suppress("deprecation")
                    CLIENT.getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEX).apply(this.overlayKind.sprites?.sides)
                }
                Direction.DOWN, null -> return false.also { println("3") }
            }

            Pair(sprite, this.overlayColor)
        }

        if (color != null) {
            println("Applying color ${(color and 0x00FFFFFF).toString(16)}")
            qe.spriteColor(0, color, color, color, color)
        }

        applySprite(qe, sprite, u, v)

        return true
    }
}