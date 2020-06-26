package dev.alexnader.framity.model

import com.mojang.datafixers.util.Pair
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.ModelBakeSettings
import net.minecraft.client.render.model.ModelLoader
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.util.Identifier
import java.util.function.Function

class UnbakedDelegatedModel constructor(
    private val delegateId: ModelIdentifier,
    private val sprites: List<SpriteIdentifier>
) : UnbakedModel {
    private lateinit var model: BakedModel

    override fun getModelDependencies(): Collection<Identifier> = listOf(this.delegateId)

    override fun getTextureDependencies(
        unbakedModelGetter: Function<Identifier, UnbakedModel>?,
        unresolvedTextureReferences: MutableSet<Pair<String, String>>?
    ): Collection<SpriteIdentifier> = this.sprites

    override fun bake(
        loader: ModelLoader,
        textureGetter: Function<SpriteIdentifier, Sprite>,
        rotationContainer: ModelBakeSettings?,
        modelId: Identifier?
    ): BakedModel? {
        if (!this::model.isInitialized) {
            val delegateModel = loader.bakedModelMap[this.delegateId] ?: loader.getOrLoadModel(this.delegateId).bake(loader, textureGetter, rotationContainer, this.delegateId)!!
            this.model = BakedDelegateModel(delegateModel)
        }
        return this.model
    }
}
