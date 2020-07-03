package dev.alexnader.framity.client.model

import net.fabricmc.fabric.api.client.model.ModelProviderContext
import net.fabricmc.fabric.api.client.model.ModelVariantProvider
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.render.block.BlockModels
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.state.property.Property
import net.minecraft.util.registry.Registry

class FramityModelVariantProvider : ModelVariantProvider {
    companion object {
        private fun <T : Comparable<T>> copyProperty(prop: Property<T>, from: BlockState, to: BlockState): BlockState {
            return to.with(prop, from[prop])
        }

        private fun <T> cartesianProduct(sets: Collection<Set<T>>): Set<List<T>> =
            sets
                .fold(listOf(listOf<T>())) { acc, set ->
                    acc.flatMap { list -> set.map { element -> list + element } }
                }
                .toSet()
    }

    private val variants: MutableMap<ModelIdentifier, UnbakedModel> = mutableMapOf()

    fun registerModelsFor(targetBlock: Block, delegateBlock: Block, sprites: List<SpriteIdentifier>) {
        val unimportantProps = targetBlock.stateManager.defaultState.properties subtract delegateBlock.stateManager.defaultState.properties

        val unimportantProduct =
            cartesianProduct(
                unimportantProps.map { prop -> prop.getValues().map { v -> Pair(prop, v) }.toSet() })

        delegateBlock.stateManager.states.forEach { delegateState ->
            val importantState = delegateState.properties.fold(targetBlock.stateManager.defaultState, { acc, prop ->
                @Suppress("UNCHECKED_CAST")
                (copyProperty(
        prop as Property<Comparable<Comparable<*>>>,
        delegateState,
        acc
    ))
            })

            unimportantProduct.forEach { unimportantTargetState ->
                val targetState = unimportantTargetState.fold(importantState, { acc, (prop, v) ->
                    @Suppress("UNCHECKED_CAST")
                    acc.with(prop as Property<Comparable<Comparable<*>>>, v as Comparable<Comparable<*>>)
                })

                val targetId = BlockModels.getModelId(targetState)
                val delegateId = BlockModels.getModelId(delegateState)
                this.variants[targetId] =
                    UnbakedDelegatedModel(delegateId, sprites)
            }
        }
        val targetItemId = Registry.ITEM.getId(targetBlock.asItem())
        val delegateItemId = Registry.ITEM.getId(delegateBlock.asItem())
        if (targetItemId == Registry.ITEM.defaultId || delegateItemId == Registry.ITEM.defaultId) {
            return
        }
        val targetId = ModelIdentifier(targetItemId, "inventory")
        val delegateId = ModelIdentifier(delegateItemId, "inventory")
        this.variants[targetId] =
            UnbakedDelegatedModel(delegateId, sprites)
    }

    override fun loadModelVariant(modelId: ModelIdentifier?, context: ModelProviderContext?) =
        this.variants[modelId]
}