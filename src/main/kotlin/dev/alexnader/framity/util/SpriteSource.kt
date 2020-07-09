package dev.alexnader.framity.util

import dev.alexnader.framity.mixin.AccessibleBakedQuad
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.texture.MissingSprite
import net.minecraft.client.texture.Sprite
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.util.math.Direction
import java.util.*

sealed class SpriteSource(state: BlockState?, model: BakedModel, rand: Random) {
    companion object {
        @Suppress("deprecation")
        protected val FALLBACK_SPRITE: Sprite =
            MinecraftClient.getInstance()
                .getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEX)
                .apply(MissingSprite.getMissingSpriteId())
    }

    class Default(private val defaultSprite: Sprite, state: BlockState?, model: BakedModel, rand: Random) : SpriteSource(state, model, rand) {
        override fun get(dir: Direction, index: Int) =
            defaultSprite

        override fun hasColor(dir: Direction, index: Int) =
            false
    }

    /**
     * A defaulted mutable map from [Direction] to [Sprite].
     */
    class Set(state: BlockState, model: BakedModel, rand: Random) : SpriteSource(state, model, rand) {
        override operator fun get(dir: Direction, index: Int) =
            this.getQuad(dir, index)
                ?.let { (it as AccessibleBakedQuad).sprite }
                ?: FALLBACK_SPRITE

        private fun getQuad(dir: Direction, index: Int): BakedQuad? {
            val quadList = this.quads[dir] ?: return null

            if (index >= quadList.size) {
                return null
            }

            return quadList[index]
        }

        override fun hasColor(dir: Direction, index: Int) =
            this.getQuad(dir, index)?.hasColor() ?: false
    }

    protected val quads = (0..6).asSequence()
        .map(ModelHelper::faceFromIndex)
        .map { Pair(it, model.getQuads(state, it, rand)) }
        .filter { it.second.isNotEmpty() }
        .map { Pair(it.first, it.second.toMutableList()) }
        .toMap()

    abstract operator fun get(dir: Direction, index: Int): Sprite
    abstract fun hasColor(dir: Direction, index: Int): Boolean

    fun getCount(dir: Direction) =
        this.quads[dir]?.size ?: -1
}
