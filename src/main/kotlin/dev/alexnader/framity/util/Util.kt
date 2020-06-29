package dev.alexnader.framity.util

import net.minecraft.util.Identifier
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import java.util.*

infix fun <A, B, C> ((A) -> B).andThen(f: (B) -> C): (A) -> C =
    { arg -> f(this(arg)) }

fun <T> Optional<T>.orNull(): T? =
    this.orElse(null)

/**
 * Returns the union of this and [rhs].
 */
operator fun VoxelShape.plus(rhs: VoxelShape): VoxelShape =
    VoxelShapes.union(this, rhs)

fun <N> minMax(a: N, b: N) where N: Number, N: Comparable<N> =
    if (a < b) {
        Pair(a, b)
    } else {
        Pair(b, a)
    }
