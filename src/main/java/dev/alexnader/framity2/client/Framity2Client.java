package dev.alexnader.framity2.client;

import com.mojang.serialization.Codec;
import dev.alexnader.framity2.client.assets.OverlayAssetListener;
import dev.alexnader.framity2.client.assets.overlay.OffsetterRegistry;
import dev.alexnader.framity2.client.assets.overlay.ZeroOffsetter;
import dev.alexnader.framity2.client.gui.FrameScreen;
import dev.alexnader.framity2.client.transform.FrameTransform;
import dev.alexnader.framity2.items.FramersHammer;
import grondag.jmx.api.QuadTransformRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import static dev.alexnader.framity2.Framity2.ITEMS;
import static dev.alexnader.framity2.Framity2.META;

@Environment(EnvType.CLIENT)
public class Framity2Client implements ClientModInitializer {
    public static FramityCodecs CODECS;

    public static OverlayAssetListener CLIENT_OVERLAYS;

    @Override
    public void onInitializeClient() {
        final Identifier zeroId = META.id("zero");
        OffsetterRegistry.register(zeroId, Codec.unit(new ZeroOffsetter(zeroId)));

        Framity2Client.CODECS = new FramityCodecs();

        QuadTransformRegistry.INSTANCE.register(
            META.id("frame_transform"),
            FrameTransform.SOURCE
        );

        ScreenRegistry.register(META.FRAME_SCREEN_HANDLER_TYPE, FrameScreen.FACTORY);

        //noinspection deprecation
        ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).register((atlas, registry) -> {
            final int textureStart = "textures/".length();
            final int pngLen = ".png".length();

            for (final Identifier id : MinecraftClient.getInstance().getResourceManager().findResources("textures/framity", s->s.endsWith(".png"))) {
                registry.register(new Identifier(id.getNamespace(), id.getPath().substring(textureStart, id.getPath().length() - pngLen)));
            }
        });

        CLIENT_OVERLAYS = new OverlayAssetListener();
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(CLIENT_OVERLAYS);

        ModelLoadingRegistry.INSTANCE.registerAppender(
            (resourceManager, out) -> out.accept(new ModelIdentifier(META.id("framers_hammer_none"), "inventory"))
        );

        FabricModelPredicateProviderRegistry.register(ITEMS.FRAMERS_HAMMER, META.id("hammer_mode"), FramersHammer.MODEL_PREDICATE);
    }
}
