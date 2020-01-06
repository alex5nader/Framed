package dev.alexnader.framity.model

import dev.alexnader.framity.adapters.buildMesh
import dev.alexnader.framity.util.BlockStateMap
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.state.property.Property
import java.util.function.Function

open class FramityVoxelModel(
    block: Block,
    properties: List<Property<out Comparable<*>>>,
    sprite: SpriteIdentifier,
    transformerFactory: () -> MeshTransformer,
    defaultState: BlockState,
    spriteMap: Function<SpriteIdentifier, Sprite>
) : BaseFrameModel(sprite, transformerFactory, defaultState, spriteMap) {
    override val blockStateMap = BlockStateMap<Mesh>()

    init {
        @Suppress("unchecked_cast")
        properties.forEach { blockStateMap.includeProperty(it as Property<Comparable<Any>>) }

        block.stateManager.states.forEach { state ->
            blockStateMap[state] = RENDERER.buildMesh { qe ->
                state.getOutlineShape(null, null)?.forEachBox { x1, y1, z1, x2, y2, z2 ->
                    qe.cube(
                        x1.toFloat(), y1.toFloat(), z1.toFloat(), x2.toFloat(), y2.toFloat(), z2.toFloat(), -1
                    )
                }
                qe
            }
        }
    }
}