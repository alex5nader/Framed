package dev.alexnader.framity.client.model

import dev.alexnader.framity.blocks.Frame
import dev.alexnader.framity.client.assets.*
import dev.alexnader.framity.client.assets.getValidOverlay
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.texture.Sprite
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.world.BlockRenderView
import java.util.*
import java.util.function.Supplier
import dev.alexnader.framity.util.*
import net.minecraft.client.texture.SpriteAtlasTexture
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

const val WHITE      = 0x00FFFFFF
const val FULL_ALPHA = 0xFF000000.toInt()

class FrameTransform(
    partCount: Int,
    defaultSprite: Sprite,
    blockView: BlockRenderView,
    private val state: BlockState,
    pos: BlockPos,
    randomSupplier: Supplier<Random>
) : RenderContext.QuadTransform {
    private data class Data(
        val sprites: SpriteSource,
        val overlay: OverlayInfo.Complete?,
        val cachedOverlayColor: Int?,
        val color: Int,
        val material: RenderMaterial?
    )

    companion object {
        private val CLIENT = MinecraftClient.getInstance()
        private val MAT_FINDER = RendererAccess.INSTANCE.renderer.materialFinder()
    }

    private val data: FixedSizeList<Data>
    private val transformedIndex: EnumMap<Direction, Int> = EnumMap(Direction::class.java)

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

        if (baseStates.size != partCount) {
            error("Transformer failed: expected $partCount parts, found ${baseStates.size}.")
        }

        data = FixedSizeList((0 until partCount).asSequence().map { i ->
            val baseState = baseStates[i]
            val model = CLIENT.blockRenderManager.getModel(baseState)

            val (material, color, sprites) = if (baseState == null) {
                val material = MAT_FINDER.clear().blendMode(0, BlendMode.CUTOUT).find()
                Triple(material, WHITE, SpriteSource.Default(defaultSprite, null, model, randomSupplier.get()))
            } else {
                val material = MAT_FINDER.clear().disableDiffuse(0, false).disableAo(0, false).blendMode(0, BlendMode.fromRenderLayer(RenderLayers.getBlockLayer(state))).find()

                val color = ColorProviderRegistry.BLOCK.get(baseState.block)?.let { colorProvider ->
                    FULL_ALPHA or colorProvider.getColor(baseState, blockView, pos, 1)
                } ?: WHITE
                Triple(material, color, SpriteSource.Set(baseState, model, randomSupplier.get()))
            }
            val overlay = getValidOverlay(overlayIds[i])
            val cachedOverlayColor = overlay?.coloredLike?.let { coloredLike ->
                ColorProviderRegistry.BLOCK.get(coloredLike.colorSource.block)
                    ?.getColor(coloredLike.colorSource, blockView, pos, 1)
                    ?.let { FULL_ALPHA or it }
            }
            Data(sprites, overlay, cachedOverlayColor, color, material)
        }) { error("Cannot remove from Frame Transform Data") }
    }

    override fun transform(qe: MutableQuadView): Boolean {
        val direction = qe.lightFace()!!

        val quadIndex = this.transformedIndex[direction] ?: 0
        this.transformedIndex[direction] = quadIndex + 1

        val state = this.state

        val partIndex = run {
            val xs = Float4(qe.x(0), qe.x(1), qe.x(2), qe.x(3))
            val ys = Float4(qe.y(0), qe.y(1), qe.y(2), qe.y(3))
            val zs = Float4(qe.z(0), qe.z(1), qe.z(2), qe.z(3))
            (state.block as Frame).getSlotFor(state, Vec3d(xs.center.toDouble(), ys.center.toDouble(), zs.center.toDouble()), direction)
        }

        val (sprites, overlay, cachedOverlayColor, storedColor, material) = data[partIndex]

        qe.material(material)

        val (sprite, color) = if (quadIndex % 2 == 0) {
            if (sprites is SpriteSource.Default) {
                return true
            }

            val spriteIndex = quadIndex % sprites.getCount(direction)

            val sprite = sprites[direction, spriteIndex]
            val color = if (sprites.hasColor(direction, spriteIndex)) {
                storedColor
            } else {
                null
            }

            Pair(sprite, color)
        } else {
            val spriteIndex = quadIndex % sprites.getCount(direction)

            fun handleTexture(source: TextureSource): Pair<Sprite, Int?>? {
                return when (source) {
                    is TextureSource.Sided -> {
                        val spriteId = source[direction] ?: return null
                        @Suppress("deprecation")
                        val sprite = CLIENT.getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEX).apply(spriteId)

                        Pair(sprite, cachedOverlayColor)
                    }
                    is TextureSource.Single -> {
                        @Suppress("deprecation")
                        val sprite = CLIENT.getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEX).apply(source.spriteId)

                        Pair(sprite, cachedOverlayColor)
                    }
                }
            }

            val (sprite, color) = when (overlay) {
                null -> {
                    val sprite = sprites[direction, spriteIndex]
                    val color = if (sprites.hasColor(direction, spriteIndex)) {
                        storedColor
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

        if (color != null) {
            qe.spriteColor(0, color, color, color, color)
        }

        val (uSource, vSource) = when (direction) {
            Direction.DOWN  -> Pair(qe::x,                qe::z andThen ::flip)
            Direction.UP    -> Pair(qe::x,                qe::z)
            Direction.NORTH -> Pair(qe::x andThen ::flip, qe::y andThen ::flip)
            Direction.SOUTH -> Pair(qe::x,                qe::y andThen ::flip)
            Direction.EAST  -> Pair(qe::z andThen ::flip, qe::y andThen ::flip)
            Direction.WEST  -> Pair(qe::z,                qe::y andThen ::flip)
        }

        val (us, vs) = Float4(
            uSource(0),
            uSource(1),
            uSource(2),
            uSource(3)
        ).let { origUs ->
            Float4(vSource(0), vSource(1), vSource(2), vSource(3))
                .let { origVs ->
                fun handleOffsetter(offsetter: Offsetter, orig: Float4): Float4? =
                    when (offsetter) {
                        is Offsetter.Remap ->
                            offsetter[orig] ?: orig
                        Offsetter.Zero -> {
                            val (min, max) = minMax(orig.a, orig.b)
                            val delta = max - min

                            if (orig.a == min) {
                                Float4(0f, delta, delta, 0f)
                            } else {
                                Float4(delta, 0f, 0f, delta)
                            }
                        }
                    }

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
