package dev.alexnader.framity.util

import dev.alexnader.framity.mixin.AccessibleBakedQuad
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.texture.Sprite
import net.minecraft.util.math.Direction
import java.util.*

class SpriteSource(state: BlockState?, model: BakedModel, rand: Random) {
    operator fun get(dir: Direction, index: Int) =
        this.getQuad(dir, index)
            ?.let { (it as AccessibleBakedQuad).sprite }

    fun getSpriteAndColor(direction: Direction, index: Int, color: Int): Pair<Sprite, Int?>? =
        this.getQuad(direction, index)
            ?.let { quad -> Pair(quad.sprite, color.takeIf { quad.hasColor() } ) }

    private fun getQuad(dir: Direction, index: Int): BakedQuad? {
        val quadList = this.quads[dir] ?: return null

        if (index >= quadList.size) {
            return null
        }

        return quadList[index]
    }

    fun hasColor(dir: Direction, index: Int) =
        this.getQuad(dir, index)?.hasColor() ?: false

    private val quads = (0..6).asSequence()
        .map(ModelHelper::faceFromIndex)
        .map { Pair(it, model.getQuads(state, it, rand)) }
        .filter { it.second.isNotEmpty() }
        .map { Pair(it.first, it.second.toMutableList()) }
        .toMap()

    fun getCount(dir: Direction) =
        this.quads[dir]?.size ?: -1
}
