package dev.alexnader.framity.model

import dev.alexnader.framity.data.overlay.runtime.*
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
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.world.BlockRenderView
import java.util.*
import java.util.function.Supplier
import dev.alexnader.framity.util.*
import net.minecraft.client.texture.MissingSprite
import net.minecraft.client.texture.SpriteAtlasTexture

/**
 * Flips a number in the range [0, 1] as if it were in the range [1, 0].
 */
private fun flip(f: Float) = 1 - f

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

        /**
         * Clamps a number to the range [0, 1].
         */
        private fun clamp01(f: Float) = MathHelper.clamp(f, 0f, 1f)
    }

    private val sprites = SpriteSet(defaultSprite)
    private val transformedIndex: EnumMap<Direction, Int> = EnumMap(Direction::class.java)

    private var overlay: OverlayInfo? = null
    private var cachedOverlayColor: Int? = null

    private var color = 0
    private var mat: RenderMaterial? = null

    @Suppress("UNCHECKED_CAST")
    override fun prepare(
        blockView: BlockRenderView?,
        state: BlockState?,
        pos: BlockPos?,
        randomSupplier: Supplier<Random>?
    ): MeshTransformer {
        val (containedState, overlayKind) = (blockView as RenderAttachedBlockView).getBlockEntityRenderAttachment(pos) as Pair<BlockState?, OverlayInfo?>

        if (containedState == null) {
            sprites.clear()
            this.mat = MAT_FINDER.clear().blendMode(0, BlendMode.CUTOUT).find()
        } else {
            this.mat = MAT_FINDER.clear().disableDiffuse(0, false).disableAo(0, false).blendMode(0, BlendMode.fromRenderLayer(RenderLayers.getBlockLayer(containedState))).find()
            val model = CLIENT.blockRenderManager.getModel(containedState)
            sprites.prepare(model, randomSupplier?.get())

            val colorProvider = ColorProviderRegistry.BLOCK.get(containedState.block)
            if (colorProvider != null) {
                println("Using color provider")
                this.color = FULL_ALPHA or colorProvider.getColor(containedState, blockView, pos, 1)
            } else {
                println("No color provider")
            }
        }

        fun colorHandler(coloredLike: ColoredLike): Int? =
            ColorProviderRegistry.BLOCK.get(coloredLike.colorSource.block)
                ?.getColor(coloredLike.colorSource, blockView, pos, 1)
                ?.let { FULL_ALPHA or it }

        this.cachedOverlayColor = if (overlayKind?.coloredLike != null) {
            colorHandler(overlayKind.coloredLike)
        } else {
            null
        }

        this.overlay = overlayKind

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

        val direction = qe.lightFace()!!

        val shapeIndex = this.transformedIndex[direction] ?: 0
        this.transformedIndex[direction] = shapeIndex + 1

        // first half of quads are base, second half are overlay
        val (sprite, color) = if (shapeIndex % 2 == 0) {
            if (sprites.isDefault) {
                return true
            }

            val spriteIndex = shapeIndex % this.sprites.getQuadCount(direction)

            val sprite = this.sprites[direction, spriteIndex]
            val color = if (this.sprites.hasColor(direction, spriteIndex)) {
                this.color
            } else {
                null
            }

            Pair(sprite, color)
        } else {
            val spriteIndex = shapeIndex % this.sprites.getQuadCount(direction)

            fun handleTexture(source: TextureSource): Pair<Sprite, Int?>? {
                return when (source) {
                    is TextureSource.Sided -> {
                        val spriteId = source[direction] ?: return null
                        @Suppress("deprecation")
                        val sprite = CLIENT.getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEX).apply(spriteId)

                        Pair(sprite, this.cachedOverlayColor)
                    }
                    is TextureSource.Single -> {
                        @Suppress("deprecation")
                        val sprite = CLIENT.getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEX).apply(source.spriteId)

                        Pair(sprite, this.cachedOverlayColor)
                    }
                }
            }

            val (sprite, color) = when (val overlay = this.overlay) {
                null -> {
                    val sprite = this.sprites[direction, spriteIndex]
                    val color = if (this.sprites.hasColor(direction, spriteIndex)) {
                        this.color
                    } else {
                        null
                    }

                    Pair(sprite, color)
                }
                else -> {
                    handleTexture(overlay.textureSource) ?: return false // don't render when there's no texture for this quad
                }
            }

            Pair(sprite, color)
        }

        val (uSource, vSource) = when (direction) {
            Direction.DOWN  -> Pair(qe::x,                qe::z andThen ::flip)
            Direction.UP    -> Pair(qe::x,                qe::z)
            Direction.NORTH -> Pair(qe::x andThen ::flip, qe::y andThen ::flip)
            Direction.SOUTH -> Pair(qe::x,                qe::y andThen ::flip)
            Direction.EAST  -> Pair(qe::z andThen ::flip, qe::y andThen ::flip)
            Direction.WEST  -> Pair(qe::z,                qe::y andThen ::flip)
        }

        if (color != null) {
            println("${if (shapeIndex % 2 == 0) "base" else "overlay"} $direction is colored")
            qe.spriteColor(0, color, color, color, color)
        } else {
            println("${"\u001B[33m"}${if (shapeIndex % 2 == 0) "base" else "overlay"} $direction has no color${"\u001B[0m"}")
        }

        if (sprite.id == MissingSprite.getMissingSpriteId()) {
            println("$direction face #$shapeIndex is missing - provided by ${if (shapeIndex % 2 == 0) "base" else "overlay"}")
        }

        val (us, vs) = Pair(
            Float4(
                uSource(0),
                uSource(1),
                uSource(2),
                uSource(3)
            ),
            Float4(
                vSource(0),
                vSource(1),
                vSource(2),
                vSource(3)
            )
        ).let { (origUs, origVs) ->
            fun handleOffsetter(offsetter: Offsetter, orig: Float4): Float4? =
                when (offsetter) {
                    is Offsetter.Remap ->
                        offsetter[orig] ?: orig
                }

            val overlay = this.overlay
            val offsets = if (overlay?.offsets != null) {
                overlay.offsets[direction]?.let { offsetters ->
                    when (offsetters) {
                        is Offsetters.U ->
                            Pair(handleOffsetter(offsetters.uOffsetter, origUs) ?: origUs, origVs)
                        is Offsetters.V ->
                            Pair(origUs, handleOffsetter(offsetters.vOffsetter, origVs) ?: origVs)
                        is Offsetters.UV ->
                            Pair(
                                handleOffsetter(offsetters.uOffsetter, origUs) ?: origUs,
                                handleOffsetter(offsetters.vOffsetter, origVs) ?: origVs
                            )
                    }
                }
            } else {
                null
            }

            offsets ?: Pair(origUs, origVs)
        }

        qe
            .sprite(0, 0,
                MathHelper.lerp(clamp01(us.a), sprite.minU, sprite.maxU),
                MathHelper.lerp(clamp01(vs.a), sprite.minV, sprite.maxV))
            .sprite(1, 0,
                MathHelper.lerp(clamp01(us.b), sprite.minU, sprite.maxU),
                MathHelper.lerp(clamp01(vs.b), sprite.minV, sprite.maxV))
            .sprite(2, 0,
                MathHelper.lerp(clamp01(us.c), sprite.minU, sprite.maxU),
                MathHelper.lerp(clamp01(vs.c), sprite.minV, sprite.maxV))
            .sprite(3, 0,
                MathHelper.lerp(clamp01(us.d), sprite.minU, sprite.maxU),
                MathHelper.lerp(clamp01(vs.d), sprite.minV, sprite.maxV))

        return true
    }
}