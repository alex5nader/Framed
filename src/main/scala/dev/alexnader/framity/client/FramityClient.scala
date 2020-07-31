package dev.alexnader.framity.client

import dev.alexnader.framity.Framity
import dev.alexnader.framity.client.assets.overlay
import dev.alexnader.framity.client.gui.FrameScreen
import dev.alexnader.framity.item.FramersHammer
import grondag.jmx.api.QuadTransformRegistry
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.`object`.builder.v1.client.model.FabricModelPredicateProviderRegistry
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier

object FramityClient extends ClientModInitializer {
  override def onInitializeClient(): Unit = {
    QuadTransformRegistry.INSTANCE.register(Framity.Mod.id("non_frex_frame_transform"), FrameTransform.NonFrex.Source)
//    QuadTransformRegistry.INSTANCE.register(Framity.Mod.id("frex_frame_transform"), FrameTransform.Frex.Source)

    ScreenRegistry.register(Framity.FrameScreenHandlerType, FrameScreen)

    ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEX).register((_, registry) => {
      val texStart = "textures/".length
      val pngLen = ".png".length
      MinecraftClient.getInstance.getResourceManager.findResources("textures/framity", _.endsWith(".png")) forEach { texId =>
        registry.register(new Identifier(texId.getNamespace, texId.getPath.substring(texStart, texId.getPath.length - pngLen)))
      }
    })

    ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(overlay.Listener)

    ModelLoadingRegistry.INSTANCE.registerAppender((_, out) => {
      out.accept(new ModelIdentifier(Framity.Mod.id("framers_hammer_none"), "inventory"))
    })

    FabricModelPredicateProviderRegistry.register(FramersHammer, Framity.Mod.id("hammer_mode"), FramersHammer.ModelPredicate)
  }
}
