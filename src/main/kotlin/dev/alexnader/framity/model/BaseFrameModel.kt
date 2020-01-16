package dev.alexnader.framity.model

import net.fabricmc.fabric.api.renderer.v1.Renderer
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper
import net.minecraft.block.BlockState
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import java.util.function.Function

/**
 * [TransformableModel] subclass for all frame models.
 */
abstract class BaseFrameModel(
    sprite: SpriteIdentifier,
    transformerFactory: () -> MeshTransformer,
    defaultState: BlockState,
    spriteMap: Function<SpriteIdentifier, Sprite>
) : TransformableModel(
    transformerFactory,
    defaultState,
    spriteMap.apply(sprite),
    ModelHelper.MODEL_TRANSFORM_BLOCK
) {
    companion object {
        @JvmStatic
        val RENDERER: Renderer = RendererAccess.INSTANCE.renderer
    }

    /**
     * Mapping from [BlockState] to [Mesh]. Must contain a value for all possible [BlockState]s.
     *
     * @see [dev.alexnader.framity.util.BlockStateMap]
     */
    abstract val blockStateMap: Map<BlockState, Mesh>

    override fun createMesh(state: BlockState?) = this.blockStateMap[state] ?: error("No defined model for $state")
}