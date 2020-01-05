package dev.alexnader.framity.adapters

import net.fabricmc.fabric.api.renderer.v1.Renderer
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.minecraft.block.Block
import net.minecraft.client.util.math.Vector3f
import net.minecraft.item.Item
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Quaternion
import net.minecraft.util.registry.Registry
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes

inline val Item.id get() = Registry.ITEM.getId(this)
inline val Block.id get() = Registry.BLOCK.getId(this)

inline fun Renderer.buildMesh(block: (QuadEmitter) -> QuadEmitter?): Mesh {
    val builder = this.meshBuilder()

    block(builder.emitter!!)

    return builder.build()
}

inline val Direction.mask get() = 1 shl this.horizontal

inline val Direction.tag get() = this.id + 1

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

operator fun VoxelShape.plus(rhs: VoxelShape): VoxelShape =
    VoxelShapes.union(this, rhs)
