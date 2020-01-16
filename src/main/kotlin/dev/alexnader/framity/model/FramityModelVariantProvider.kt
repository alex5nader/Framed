package dev.alexnader.framity.model

import grondag.fermion.client.models.AbstractModel
import grondag.fermion.client.models.SimpleUnbakedModel
import net.fabricmc.fabric.api.client.model.ModelProviderContext
import net.fabricmc.fabric.api.client.model.ModelVariantProvider
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.render.block.BlockModels
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.util.registry.Registry
import java.util.function.Function

/**
 * [ModelVariantProvider] implementation which enables usage of custom model classes.
 */
class FramityModelVariantProvider : ModelVariantProvider {
    private val variants: MutableMap<ModelIdentifier?, UnbakedModel> = mutableMapOf()

    override fun loadModelVariant(identifier: ModelIdentifier?, ctx: ModelProviderContext?) = variants[identifier]

    /**
     * Creates a new model for each block state of [block] and for [itemState].
     */
    fun registerModels(
        block: Block,
        itemState: BlockState,
        modelConstructor: (BlockState, Function<SpriteIdentifier, Sprite>) -> AbstractModel,
        sprites: List<SpriteIdentifier>
    ) {
        for (state in block.stateManager.states) {
            variants[BlockModels.getModelId(state)] =
                SimpleUnbakedModel({ spriteMap -> modelConstructor(state, spriteMap) }, sprites)
        }
        variants[ModelIdentifier(Registry.ITEM.getId(block.asItem()), "inventory")] =
            SimpleUnbakedModel({ spriteMap -> modelConstructor(itemState, spriteMap) }, sprites)
    }
}