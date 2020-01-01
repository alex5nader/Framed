package dev.alexnader.framity.model

import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.texture.Sprite
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockRenderView
import java.util.*
import java.util.function.Supplier

abstract class TransformableModel(
    private val transformerFactory: (() -> MeshTransformer)?,
    defaultState: BlockState,
    sprite: Sprite,
    transformation: ModelTransformation
) : StatefulModel(
    defaultState, sprite, transformation
) {
    private fun transformed(context: RenderContext?, transformer: MeshTransformer?, block: () -> Unit) {
        transformer?.let { context?.pushTransform(it) }
        block()
        transformer?.let { context?.popTransform() }
    }

    override fun emitItemQuads(stack: ItemStack?, randomSupplier: Supplier<Random>?, context: RenderContext?) {
        transformed(context, this.transformerFactory?.invoke()?.prepare(stack, randomSupplier)) {
            super.emitItemQuads(stack, randomSupplier, context)
        }
    }

    override fun emitBlockQuads(
        blockView: BlockRenderView?,
        state: BlockState?,
        pos: BlockPos?,
        randomSupplier: Supplier<Random>?,
        context: RenderContext?
    ) {
        transformed(context, this.transformerFactory?.invoke()?.prepare(blockView, state, pos, randomSupplier)) {
            super.emitBlockQuads(blockView, state, pos, randomSupplier, context)
        }
    }
}