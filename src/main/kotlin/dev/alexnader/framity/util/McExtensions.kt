package dev.alexnader.framity.util

import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes

/**
 * Returns the union of this and [rhs].
 */
operator fun VoxelShape.plus(rhs: VoxelShape): VoxelShape =
    VoxelShapes.union(this, rhs)
