package dev.alexnader.framity

import com.google.gson.Gson
import dev.alexnader.framity.block_entities.FrameEntity
import dev.alexnader.framity.util.WithId
import dev.alexnader.framity.blocks.*
import dev.alexnader.framity.client.assets.OverlayAssetsListener
import dev.alexnader.framity.client.gui.FrameGuiDescription
import dev.alexnader.framity.client.gui.FrameScreen
import dev.alexnader.framity.items.FramersHammer
import dev.alexnader.framity.client.model.FrameTransform
import dev.alexnader.framity.data.OverlayDataListener
import grondag.jmx.api.QuadTransformRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.`object`.builder.v1.client.model.FabricModelPredicateProviderRegistry
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry
import net.minecraft.block.*
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.resource.ResourceType
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

val GSON = Gson()

val LOGGER: Logger = LogManager.getLogger("Framity")

@JvmField
val MOD = Mod("framity")
    .itemGroup("framity") { ItemStack(FRAMERS_HAMMER.value) }
    .done()

@Suppress("unused")
val UV_TEST = MOD.block("uv_test", Block(FabricBlockSettings.of(Material.STONE)))
    .hasItem(Item.Settings())
    .done()

@JvmField
val FRAMERS_HAMMER: WithId<Item> = MOD.item("framers_hammer", FramersHammer())
    .itemGroup("framity")
    .done()

val BLOCK_FRAME = MOD.block("block_frame", BlockFrame())
    .hasItem(Item.Settings(), "framity")
    .renderLayer(RenderLayer.getCutout())
    .done()
val SLAB_FRAME = MOD.block("slab_frame", SlabFrame())
    .hasItem(Item.Settings(), "framity")
    .renderLayer(RenderLayer.getCutout())
    .done()
val STAIRS_FRAME = MOD.block("stairs_frame", StairsFrame())
    .hasItem(Item.Settings(), "framity")
    .renderLayer(RenderLayer.getCutout())
    .done()
val FENCE_FRAME = MOD.block("fence_frame", FenceFrame())
    .hasItem(Item.Settings(), "framity")
    .renderLayer(RenderLayer.getCutout())
    .done()
val FENCE_GATE_FRAME = MOD.block("fence_gate_frame", FenceGateFrame())
    .hasItem(Item.Settings(), "framity")
    .renderLayer(RenderLayer.getCutout())
    .done()
val TRAPDOOR_FRAME = MOD.block("trapdoor_frame", TrapdoorFrame())
    .hasItem(Item.Settings(), "framity")
    .renderLayer(RenderLayer.getCutout())
    .done()

val FRAME_ENTITY = MOD.blockEntity("frame_entity", ::FrameEntity,
    BLOCK_FRAME.value,
    STAIRS_FRAME.value,
    FENCE_FRAME.value,
    FENCE_GATE_FRAME.value,
    TRAPDOOR_FRAME.value
)
val SLAB_FRAME_ENTITY = MOD.blockEntity<FrameEntity>("slab_frame_entity", { type -> FrameEntity(type, SlabFrame.FORMAT) }, SLAB_FRAME.value)

lateinit var FRAME_SCREEN_HANDLER_TYPE: ScreenHandlerType<FrameGuiDescription>

@Suppress("unused")
fun init() {
    MOD.register()

    FRAME_SCREEN_HANDLER_TYPE = ScreenHandlerRegistry.registerExtended(MOD.id("frame_screen_handler")) { syncId, inventory, buf ->
        FrameGuiDescription(syncId, inventory, ScreenHandlerContext.create(inventory.player.world, buf.readBlockPos()))
    }

    ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(OverlayDataListener)
}

@Suppress("unused")
fun clientInit() {
    MOD.registerClient()

    QuadTransformRegistry.INSTANCE.register(MOD.id("non_frex_frame_transform"), FrameTransform.NonFrex.Source)
    QuadTransformRegistry.INSTANCE.register(MOD.id("frex_frame_transform"), FrameTransform.Frex.Source)

    ScreenRegistry.register(FRAME_SCREEN_HANDLER_TYPE, FrameScreen)

    @Suppress("deprecation")
    ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEX).register(ClientSpriteRegistryCallback { _, registry -> with(registry) {
        val texStart = "textures/".length
        val pngLen = ".png".length

        val resourceManager = MinecraftClient.getInstance().resourceManager
        resourceManager.findResources("textures/framity") { s -> s.endsWith(".png") }.forEach { tex ->
            register(Identifier(tex.namespace, tex.path.substring(texStart, tex.path.length - pngLen)))
        }

        register(MOD.id("block/snow_side_overlay"))
        register(MOD.id("block/mycelium_side_overlay"))
        register(MOD.id("block/hay_side_overlay"))
        register(MOD.id("block/path_side_overlay"))
        register(MOD.id("block/crimson_nylium_side_overlay"))
        register(MOD.id("block/warped_nylium_side_overlay"))
    }})

    ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(OverlayAssetsListener)

    ModelLoadingRegistry.INSTANCE.registerAppender { _, out ->
        out.accept(ModelIdentifier(MOD.id("framers_hammer_none"), "inventory"))
    }

    FabricModelPredicateProviderRegistry.register(FRAMERS_HAMMER.value, MOD.id("hammer_mode"), FramersHammer.Companion.ModelPredicate)
}
