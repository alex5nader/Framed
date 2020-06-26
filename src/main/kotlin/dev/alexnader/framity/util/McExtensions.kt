package dev.alexnader.framity.util

import net.minecraft.client.render.VertexFormats
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes

/**
 * Returns the union of this and [rhs].
 */
operator fun VoxelShape.plus(rhs: VoxelShape): VoxelShape =
    VoxelShapes.union(this, rhs)

private val VERTEX_STRIDE = VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.vertexSizeInteger
