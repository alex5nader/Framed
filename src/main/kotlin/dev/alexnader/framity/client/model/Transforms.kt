package dev.alexnader.framity.client.model

import dev.alexnader.framity.blocks.Frame
import dev.alexnader.framity.client.assets.*
import dev.alexnader.framity.client.assets.getValidOverlay
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.Sprite
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.world.BlockRenderView
import java.util.*
import java.util.function.Supplier
import dev.alexnader.framity.util.*
import grondag.jmx.api.QuadTransformRegistry
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView
import net.minecraft.client.render.block.BlockModels
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d

/**
 * Flips a number in the range [0, 1] as if it were in the range [1, 0].
 */
private fun flip(f: Float) = 1 - f

/**
 * Clamps a number to the range [0, 1].
 */
private fun clamp01(f: Float) = MathHelper.clamp(f, 0f, 1f)

private fun MutableQuadView.sprite(sprite: Sprite, us: Float4, vs: Float4, spriteIndex: Int) {
    sprite(0, spriteIndex, MathHelper.lerp(us.a, sprite.minU, sprite.maxU), MathHelper.lerp(vs.a, sprite.minV, sprite.maxV))
    sprite(1, spriteIndex, MathHelper.lerp(us.b, sprite.minU, sprite.maxU), MathHelper.lerp(vs.b, sprite.minV, sprite.maxV))
    sprite(2, spriteIndex, MathHelper.lerp(us.c, sprite.minU, sprite.maxU), MathHelper.lerp(vs.c, sprite.minV, sprite.maxV))
    sprite(3, spriteIndex, MathHelper.lerp(us.d, sprite.minU, sprite.maxU), MathHelper.lerp(vs.d, sprite.minV, sprite.maxV))
}

private fun QuadView.getUvs(direction: Direction): Pair<Float4, Float4> {
    val (uSource, vSource) = when (direction) {
        Direction.DOWN  -> Pair(this::x andThen ::clamp01,                this::z andThen ::clamp01 andThen ::flip)
        Direction.UP    -> Pair(this::x andThen ::clamp01,                this::z andThen ::clamp01)
        Direction.NORTH -> Pair(this::x andThen ::clamp01 andThen ::flip, this::y andThen ::clamp01 andThen ::flip)
        Direction.SOUTH -> Pair(this::x andThen ::clamp01,                this::y andThen ::clamp01 andThen ::flip)
        Direction.EAST  -> Pair(this::z andThen ::clamp01 andThen ::flip, this::y andThen ::clamp01 andThen ::flip)
        Direction.WEST  -> Pair(this::z andThen ::clamp01,                this::y andThen ::clamp01 andThen ::flip)
    }

    return Pair(Float4(uSource(0), uSource(1), uSource(2), uSource(3)), Float4(vSource(0), vSource(1), vSource(2), vSource(3)))
}

const val WHITE      = 0x00FFFFFF
const val FULL_ALPHA = 0xFF000000.toInt()

sealed class FrameTransform(
    blockView: BlockRenderView,
    protected val state: BlockState,
    pos: BlockPos,
    randomSupplier: Supplier<Random>
) : RenderContext.QuadTransform {
    protected data class Data(
        val sprites: SpriteSource?,
        val overlay: OverlayInfo.Complete?,
        val cachedOverlayColor: Int?,
        val color: Int
    )

    companion object {
        protected val CLIENT: MinecraftClient = MinecraftClient.getInstance()

        fun getSpriteOrNull(direction: Direction, source: TextureSource): Sprite? {
            return when (source) {
                is TextureSource.Sided -> {
                    val spriteId = source[direction] ?: return null

                    @Suppress("deprecation")
                    CLIENT.getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEX).apply(spriteId)
                }
                is TextureSource.Single -> {
                    @Suppress("deprecation")
                    CLIENT.getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEX).apply(source.spriteId)
                }
            }
        }
    }

    protected val data: FixedSizeList<Data>

    init {
        @Suppress("UNCHECKED_CAST")
        val attachment = (blockView as RenderAttachedBlockView).getBlockEntityRenderAttachment(pos) as? Pair<FixedSizeList<BlockState?>, List<Identifier?>>? ?: error("Block (${state.block})at $pos has invalid render attachment")
        val (baseStates, overlayIds) = attachment

        if (state.block !is Frame) {
            error("Cannot  transform non-frame block ${state.block}")
        }

        if (baseStates.size != overlayIds.size) {
            error("Invalid render attachment: number of base states ${baseStates.size} must equal number of overlays ${overlayIds.size}")
        }

        data = FixedSizeList(baseStates.indices.asSequence().map { i ->
            val baseState = baseStates[i]
            val model = CLIENT.blockRenderManager.getModel(baseState)

            val (color, sprites) = if (baseState == null) {
                Pair(WHITE, null)
            } else {
                val color = ColorProviderRegistry.BLOCK.get(baseState.block)?.let { colorProvider ->
                    FULL_ALPHA or colorProvider.getColor(baseState, blockView, pos, 1)
                } ?: WHITE
                Pair(color, SpriteSource(baseState, model, randomSupplier.get()))
            }
            val overlay = getValidOverlay(overlayIds[i])
            val cachedOverlayColor = overlay?.coloredLike?.let { coloredLike ->
                ColorProviderRegistry.BLOCK.get(coloredLike.colorSource.block)
                    ?.getColor(coloredLike.colorSource, blockView, pos, 1)
                    ?.let { FULL_ALPHA or it }
            }
            Data(sprites, overlay, cachedOverlayColor, color)
        }) { error("Cannot remove from Frame Transform Data") }
    }

    protected fun getPartIndex(qe: MutableQuadView, direction: Direction): Int {
        val state = this.state

        val xs = Float4(qe.x(0), qe.x(1), qe.x(2), qe.x(3))
        val ys = Float4(qe.y(0), qe.y(1), qe.y(2), qe.y(3))
        val zs = Float4(qe.z(0), qe.z(1), qe.z(2), qe.z(3))

        return (state.block as Frame).getSlotFor(
            state,
            Vec3d(xs.center.toDouble(), ys.center.toDouble(), zs.center.toDouble()),
            direction
        )
    }

    class NonFrex(blockView: BlockRenderView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>) :
        FrameTransform(blockView, state, pos, randomSupplier)
    {
        private val transformedIndex: EnumMap<Direction, Int> = EnumMap(Direction::class.java)

        object Source : QuadTransformRegistry.QuadTransformSource {
            override fun getForItem(stack: ItemStack?, randomSupplier: Supplier<Random>?): Nothing? = null

            override fun getForBlock(blockView: BlockRenderView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>): RenderContext.QuadTransform =
                NonFrex(
                    blockView,
                    state,
                    pos,
                    randomSupplier
                )
        }

        override fun transform(qe: MutableQuadView): Boolean {
            val direction = qe.lightFace()!!

            val quadIndex = this.transformedIndex[direction] ?: 0
            this.transformedIndex[direction] = quadIndex + 1

            val partIndex = getPartIndex(qe, direction)

            val (maybeSprites, overlay, cachedOverlayColor, storedColor) = data[partIndex]

            val (spriteAndColor, us, vs) = run {
                val (origUs, origVs) = qe.getUvs(direction)

                if (quadIndex % 2 == 0) {
                    maybeSprites?.let { sprites ->
                        Triple(sprites.getSpriteAndColor(direction, quadIndex % sprites.getCount(direction), storedColor), origUs, origVs)
                    } ?: return true
                } else {
                    val spriteAndColor = when (overlay) {
                        null -> {
                            maybeSprites?.let { sprites ->
                                sprites.getSpriteAndColor(direction, quadIndex % sprites.getCount(direction), storedColor)
                            }
                        }
                        else -> {
                            getSpriteOrNull(direction, overlay.textureSource)
                                ?.let { Pair(it, cachedOverlayColor) }
                                ?: return false // don't render when there's no texture for this quad
                        }
                    }

                    val (us, vs) = overlay
                        ?.offsets
                        ?.get(direction)
                        ?.apply(origUs, origVs)
                        ?: Pair(origUs, origVs)

                    Triple(spriteAndColor, us, vs)
                }
            }

            when (spriteAndColor) {
                null -> qe.spriteColor(0, 0, 0, 0, 0)
                else -> spriteAndColor.let { (sprite, color) ->
                    qe.sprite(sprite, us, vs, 0)

                    if (color != null) {
                        qe.spriteColor(0, color, color, color, color)
                    }
                }
            }

            return true
        }
    }

    class Frex(blockView: BlockRenderView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>) :
        FrameTransform(blockView, state, pos, randomSupplier)
    {
        object Source : QuadTransformRegistry.QuadTransformSource {
            override fun getForItem(stack: ItemStack?, randomSupplier: Supplier<Random>?): Nothing? = null

            override fun getForBlock(blockView: BlockRenderView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>) =
                Frex(
                    blockView,
                    state,
                    pos,
                    randomSupplier
                )
        }

        override fun transform(qe: MutableQuadView): Boolean {
            val direction = qe.lightFace()!!

            val partIndex = getPartIndex(qe, direction)

            val (maybeSprites, overlay, cachedOverlayColor, storedColor) = data[partIndex]

            val (origUs, origVs) = qe.getUvs(direction)

            //region Base
            maybeSprites?.let { sprites ->
                val spriteAndColor = sprites.getSpriteAndColor(direction, 0, storedColor)

                if (spriteAndColor != null) {
                    val (sprite, color) = spriteAndColor
                    qe.sprite(sprite, origUs, origVs, 0)

                    if (color != null) {
                        qe.spriteColor(0, color, color, color, color)
                    }
                }
            }
            //endregion Base

            //region Overlay
            val spriteAndColor = when (overlay) {
                null -> {
                    maybeSprites?.getSpriteAndColor(direction, 1, storedColor)
                }
                else -> {
                    getSpriteOrNull(direction, overlay.textureSource)
                        ?.let { Pair(it, cachedOverlayColor) }
                }
            }
            when (spriteAndColor) {
                null -> qe.spriteColor(1, 0, 0, 0, 0)
                else -> spriteAndColor.let { (sprite, color) ->
                    val (us, vs) = overlay
                        ?.offsets
                        ?.get(direction)
                        ?.apply(origUs, origVs)
                        ?: Pair(origUs, origVs)

                    qe.sprite(sprite, us, vs, 1)

                    if (color != null) {
                        qe.spriteColor(1, color, color, color, color)
                    }
                }
            }
            //endregion Overlay

            return true
        }
    }
}
