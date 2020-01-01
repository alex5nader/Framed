package dev.alexnader.framity.adapters

import net.fabricmc.fabric.api.renderer.v1.Renderer
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.util.registry.Registry

inline val Item.id get() = Registry.ITEM.getId(this)
inline val Block.id get() = Registry.BLOCK.getId(this)

inline fun Renderer.buildMesh(block: (QuadEmitter) -> QuadEmitter?): Mesh {
    val builder = this.meshBuilder()

    block(builder.emitter!!)

    return builder.build()
}
