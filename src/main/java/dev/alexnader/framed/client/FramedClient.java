package dev.alexnader.framed.client;

import com.mojang.serialization.Codec;
import dev.alexnader.framed.client.assets.OverlayAssetListener;
import dev.alexnader.framed.client.assets.overlay.OffsetterRegistry;
import dev.alexnader.framed.client.assets.overlay.ZeroOffsetter;
import dev.alexnader.framed.client.gui.FrameScreen;
import dev.alexnader.framed.client.model.FrameModelVariantProvider;
import dev.alexnader.framed.client.transform.FrameTransform;
import dev.alexnader.framed.items.FramersHammer;
import grondag.jmx.api.QuadTransformRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.util.Optional;

import static dev.alexnader.framed.Framed.BLOCKS;
import static dev.alexnader.framed.Framed.ITEMS;
import static dev.alexnader.framed.Framed.META;

@Environment(EnvType.CLIENT)
public class FramedClient implements ClientModInitializer {
    public static FramedCodecs CODECS;

    public static OverlayAssetListener CLIENT_OVERLAYS;

    @Override
    public void onInitializeClient() {
        final Identifier zeroId = META.id("zero");
        OffsetterRegistry.register(zeroId, Codec.unit(new ZeroOffsetter(zeroId)));

        FramedClient.CODECS = new FramedCodecs();

        QuadTransformRegistry.INSTANCE.register(
            META.id("frame_transform"),
            FrameTransform.SOURCE
        );

        ScreenRegistry.register(META.FRAME_SCREEN_HANDLER_TYPE, FrameScreen.FACTORY);

        //noinspection deprecation
        ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).register((atlas, registry) -> {
            final int textureStart = "textures/".length();
            final int pngLen = ".png".length();

            for (final Identifier id : MinecraftClient.getInstance().getResourceManager().findResources("textures/framed", s->s.endsWith(".png"))) {
                registry.register(new Identifier(id.getNamespace(), id.getPath().substring(textureStart, id.getPath().length() - pngLen)));
            }
        });

        CLIENT_OVERLAYS = new OverlayAssetListener();
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(CLIENT_OVERLAYS);

        ModelLoadingRegistry.INSTANCE.registerModelProvider(
            (resourceManager, out) -> out.accept(new ModelIdentifier(META.id("framers_hammer_none"), "inventory"))
        );

        FabricModelPredicateProviderRegistry.register(
            ITEMS.FRAMERS_HAMMER,
            META.id("hammer_mode"),
            (stack, world, entity) ->
                Optional.ofNullable(stack.getTag())
                    .map(t -> t.getString("mode"))
                    .flatMap(FramersHammer.CopyMode::fromString)
                    .orElse(FramersHammer.CopyMode.DEFAULT)
                    .id
        );

        ModelLoadingRegistry.INSTANCE.registerVariantProvider(resourceManager -> {
            FrameModelVariantProvider mvp = new FrameModelVariantProvider();

            mvp.useFrameModelFor(
                BLOCKS.BLOCK_FRAME,
                BLOCKS.SLAB_FRAME,
                BLOCKS.STAIRS_FRAME,
                BLOCKS.FENCE_FRAME,
                BLOCKS.FENCE_GATE_FRAME,
                BLOCKS.TRAPDOOR_FRAME,
                BLOCKS.DOOR_FRAME,
                BLOCKS.PATH_FRAME,
                BLOCKS.TORCH_FRAME,
                BLOCKS.WALL_TORCH_FRAME,
                BLOCKS.PRESSURE_PLATE_FRAME,
                BLOCKS.WALL_FRAME,
                BLOCKS.LAYER_FRAME,
                BLOCKS.CARPET_FRAME,
                BLOCKS.PANE_FRAME
            );

            return mvp;
        });

        WorldRenderEvents.AFTER_ENTITIES.register(FramePreviewOutline::renderPreviewOutline);
    }
}
