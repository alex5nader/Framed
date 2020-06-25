//package dev.alexnader.framity.model
////
////import grondag.fermion.client.models.AbstractModel
////import grondag.fermion.client.models.SimpleUnbakedModel
////import net.fabricmc.fabric.api.client.model.ModelProviderContext
////import net.fabricmc.fabric.api.client.model.ModelVariantProvider
////import net.minecraft.block.Block
////import net.minecraft.block.BlockState
////import net.minecraft.client.render.model.UnbakedModel
////import net.minecraft.client.util.ModelIdentifier
////import net.minecraft.client.render.block.BlockModels
////import net.minecraft.client.render.model.json.JsonUnbakedModel
////import net.minecraft.client.texture.Sprite
////import net.minecraft.client.util.SpriteIdentifier
////import net.minecraft.resource.ResourceManager
////import net.minecraft.util.Identifier
////import net.minecraft.util.registry.Registry
////import java.io.InputStreamReader
////import java.lang.RuntimeException
////import java.util.function.Function
////
/////**
//// * [ModelVariantProvider] implementation which enables usage of custom model classes.
//// */
////class FramityModelVariantProvider : ModelVariantProvider {
////    companion object {
////        val models: MutableMap<ModelIdentifier?, UnbakedModel> = mutableMapOf()
////    }
////
////    private lateinit var resourceManager: ResourceManager
////
////    fun useResourceManager(resourceManager: ResourceManager): FramityModelVariantProvider {
////        this.resourceManager = resourceManager
////        return this
////    }
////
////    override fun loadModelVariant(identifier: ModelIdentifier?, ctx: ModelProviderContext?) = models[identifier]
////
////    /**
////     * Creates a new model for each block state of [block] and for [itemState].
////     */
////    fun registerModels(block: Block) {
////        for (state in block.stateManager.states) {
////            val id = BlockModels.getModelId(state)
////            models[id] =
////                UnbakedJsonFrameModel(
////                    JsonUnbakedModel.deserialize(
////                        InputStreamReader(
////                            this.resourceManager.getResource(
////                                Identifier(id.namespace, id.path)
////                            ).inputStream
////                        )
////                    )
////                )
////        }
////        val id = ModelIdentifier(Registry.ITEM.getId(block.asItem()), "inventory")
////        models[id] =
////            UnbakedJsonFrameModel(
////                JsonUnbakedModel.deserialize(
////                    InputStreamReader(
////                        this.resourceManager.getResource(
////                            Identifier(id.namespace, id.path)
////                        ).inputStream
////                    )
////                )
////            )
////    }
////}