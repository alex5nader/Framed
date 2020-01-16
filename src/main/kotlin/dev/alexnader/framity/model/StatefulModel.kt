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

/**
 * [AbstractModel] subclass providing the ability to generate a [Mesh] based on a [BlockState].
 *
 * @param defaultState The default [BlockState] to use with [createMesh].
 * @param sprite The sprite to expose via [BakedModel.getSprite].
 * @param transformation The transformation to expose via [BakedModel.getTransformation].
 */
abstract class StatefulModel(private val defaultState: BlockState?, sprite: Sprite, transformation: ModelTransformation) : AbstractModel(
    sprite,
    transformation
) {
    /**
     * Creates a [Mesh] based on the given [BlockState].
     */
    protected abstract fun createMesh(state: BlockState?): Mesh

    /**
     * This model's [Mesh]. Lazily initialized by [getMesh].
     */
    private lateinit var mesh: Mesh
    /**
     * This model's quad lists. Not strongly maintained.
     */
    private var quadLists: WeakReference<Array<MutableList<BakedQuad>>>? = null
    /**
     * This model's [ModelItemPropertyOverrideList] instance. Does nothing.
     */
    private val itemProxy = ItemProxy()

    override fun isVanillaAdapter() = false
    override fun getItemPropertyOverrides() = this.itemProxy

    /**
     * Lazy getter for [mesh]. Generates value if not yet initialized.
     */
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
        context?.meshConsumer()?.accept(this.getMesh(this.defaultState))
    }

    override fun emitBlockQuads(
        blockView: BlockRenderView?,
        state: BlockState?,
        pos: BlockPos?,
        randomSupplier: Supplier<Random>?,
        context: RenderContext?
    ) {
        context?.meshConsumer()?.accept(this.getMesh(this.defaultState))
    }

    /**
     * [ModelItemPropertyOverrideList] subclass which does nothing.
     */
    inner class ItemProxy : ModelItemPropertyOverrideList(null, null, null, emptyList()) {
        override fun apply(model: BakedModel?, stack: ItemStack?, world: World?, entity: LivingEntity?): BakedModel? {
            return this@StatefulModel
        }
    }
}