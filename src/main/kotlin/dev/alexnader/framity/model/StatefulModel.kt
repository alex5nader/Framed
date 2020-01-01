package dev.alexnader.framity.model

import grondag.fermion.client.models.AbstractModel
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.render.model.json.ModelItemPropertyOverrideList
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.texture.Sprite
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockRenderView
import net.minecraft.world.World
import java.lang.ref.WeakReference
import java.util.*
import java.util.function.Supplier

abstract class StatefulModel(private val state: BlockState?, sprite: Sprite, transformation: ModelTransformation) : AbstractModel(
    sprite,
    transformation
) {
    protected abstract fun createMesh(state: BlockState?): Mesh

    private lateinit var mesh: Mesh
    private var quadLists: WeakReference<Array<MutableList<BakedQuad>>>? = null
    private val itemProxy = ItemProxy()

    override fun isVanillaAdapter() = false
    override fun getItemPropertyOverrides() = this.itemProxy

    private fun getMesh(state: BlockState?): Mesh {
        if (!this::mesh.isInitialized)
            this.mesh = this.createMesh(state)
        return this.mesh
    }

    override fun getQuads(state: BlockState?, face: Direction?, random: Random?): MutableList<BakedQuad> {
        var quadLists = this.quadLists?.get()
        if (quadLists == null) {
            quadLists = ModelHelper.toQuadLists(this.getMesh(state))
            this.quadLists = WeakReference(quadLists)
        }
        return quadLists?.get(face?.id ?: 6) ?: mutableListOf()
    }

    override fun emitItemQuads(stack: ItemStack?, randomSupplier: Supplier<Random>?, context: RenderContext?) {
        context?.meshConsumer()?.accept(this.getMesh(this.state))
    }

    override fun emitBlockQuads(
        blockView: BlockRenderView?,
        state: BlockState?,
        pos: BlockPos?,
        randomSupplier: Supplier<Random>?,
        context: RenderContext?
    ) {
        context?.meshConsumer()?.accept(this.getMesh(this.state))
    }

    inner class ItemProxy : ModelItemPropertyOverrideList(null, null, null, emptyList()) {
        override fun apply(model: BakedModel?, stack: ItemStack?, world: World?, entity: LivingEntity?): BakedModel? {
            return this@StatefulModel
        }
    }
}