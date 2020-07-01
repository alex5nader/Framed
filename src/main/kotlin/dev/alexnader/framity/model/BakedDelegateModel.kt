package dev.alexnader.framity.model

import dev.alexnader.framity.HOLLOW_FRAME_ID
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.BakedModel
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockRenderView
import java.util.*
import java.util.function.Supplier

class BakedDelegateModel(private val delegateModel: BakedModel) : BakedModel by delegateModel, FabricBakedModel by (delegateModel as FabricBakedModel) {
    companion object {
        private fun transformed(context: RenderContext?, transformer: MeshTransformer?, block: () -> Unit) {
            transformer?.let { context?.pushTransform(it) }
            block()
            transformer?.let { context?.popTransform() }
        }

        fun makeTransformer(): MeshTransformer =
            FrameMeshTransformer(HOLLOW_FRAME_ID.sprite)
    }

    override fun emitItemQuads(stack: ItemStack?, randomSupplier: Supplier<Random>?, context: RenderContext?) {
        transformed(context, makeTransformer().apply { prepare(stack, randomSupplier) }) {
            (delegateModel as FabricBakedModel).emitItemQuads(stack, randomSupplier, context)
        }
    }

    override fun emitBlockQuads(
        blockView: BlockRenderView?,
        state: BlockState?,
        pos: BlockPos?,
        randomSupplier: Supplier<Random>?,
        context: RenderContext?
    ) {
        transformed(context, makeTransformer().apply { prepare(blockView, state, pos, randomSupplier) }) {
            (delegateModel as FabricBakedModel).emitBlockQuads(blockView, state, pos, randomSupplier, context)
        }
    }
}