package dev.alexnader.framity.adapters

import net.fabricmc.fabric.api.renderer.v1.Renderer
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.minecraft.block.enums.StairShape
import net.minecraft.client.util.math.Vector3f
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Quaternion
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes

/**
 * Scope function which handles [MeshBuilder][net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder]
 * creation and [Mesh] building.
 */
inline fun Renderer.buildMesh(block: (QuadEmitter) -> QuadEmitter?): Mesh {
    val builder = this.meshBuilder()

    block(builder.emitter!!)

    return builder.build()
}

/**
 * Gets this [Direction]'s bitwise-compatible mask.
 * Only defined for horizontal directions.
 */
inline val Direction.mask get() = 1 shl this.horizontal

/**
 * Gets this [Direction]'s tag for use when emitting quads.
 */
inline val Direction.tag get() = this.id + 1

/**
 * Returns a mirrored version of this [StairShape].
 */
inline val StairShape.mirrored get() = when (this) {
    StairShape.INNER_RIGHT -> StairShape.INNER_LEFT
    StairShape.INNER_LEFT -> StairShape.INNER_RIGHT
    StairShape.OUTER_RIGHT -> StairShape.OUTER_LEFT
    StairShape.OUTER_LEFT -> StairShape.OUTER_RIGHT
    else -> this
}

/**
 * Returns a copy of this [VoxelShape], rotated by [degrees].
 */
fun VoxelShape.rotated(degrees: Float): VoxelShape {
    var final = VoxelShapes.empty()
    this.forEachBox { x1, y1, z1, x2, y2, z2 ->
        val start = Vector3f(x1.toFloat() - 0.5f, y1.toFloat() - 0.5f, z1.toFloat() - 0.5f)
        val end = Vector3f(x2.toFloat() - 0.5f, y2.toFloat() - 0.5f, z2.toFloat() - 0.5f)

        val rotation = Quaternion(Vector3f.POSITIVE_Y, degrees, true)

        start.rotate(rotation)
        end.rotate(rotation)

        final += VoxelShapes.cuboid(start.x.toDouble() + 0.5, start.y.toDouble() + 0.5, start.z.toDouble() + 0.5, end.x.toDouble() + 0.5, end.y.toDouble() + 0.5, end.z.toDouble() + 0.5)
    }
    return final
}

/**
 * Returns the union of this and [rhs].
 */
operator fun VoxelShape.plus(rhs: VoxelShape): VoxelShape =
    VoxelShapes.union(this, rhs)
