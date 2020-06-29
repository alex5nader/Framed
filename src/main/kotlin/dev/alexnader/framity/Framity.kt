package dev.alexnader.framity

import com.google.gson.Gson
import dev.alexnader.framity.util.WithId
import dev.alexnader.framity.block_entities.FrameEntity
import dev.alexnader.framity.blocks.*
import dev.alexnader.framity.data.FramityAssetsListener
import dev.alexnader.framity.data.FramityDataListener
import dev.alexnader.framity.gui.FrameGuiDescription
import dev.alexnader.framity.gui.FrameScreen
import dev.alexnader.framity.items.FramersHammer
import dev.alexnader.framity.model.FramityModelVariantProvider
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry
import net.minecraft.block.*
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.resource.ResourceType
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

val GSON = Gson()

val LOGGER: Logger = LogManager.getLogger("Framity")

val MOD = Mod("framity", FramityModelVariantProvider())
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

val SHAPE_BLOCK_FRAME = MOD.block("shape_block_frame", Block(SHAPE_FRAME_SETTINGS))
    .hasItem(Item.Settings())
    .renderLayer(RenderLayer.getCutout())
    .done()
val SHAPE_SLAB_FRAME = MOD.block("shape_slab_frame", SlabBlock(SHAPE_FRAME_SETTINGS))
    .hasItem(Item.Settings())
    .renderLayer(RenderLayer.getCutout())
    .done()
val SHAPE_STAIRS_FRAME = MOD.block("shape_stairs_frame", Stairs(SHAPE_BLOCK_FRAME.value.defaultState, SHAPE_FRAME_SETTINGS))
    .hasItem(Item.Settings())
    .renderLayer(RenderLayer.getCutout())
    .done()
val SHAPE_FENCE_FRAME = MOD.block("shape_fence_frame", FenceBlock(SHAPE_FRAME_SETTINGS))
    .hasItem(Item.Settings())
    .renderLayer(RenderLayer.getCutout())
    .done()
val SHAPE_FENCE_GATE_FRAME = MOD.block("shape_fence_gate_frame", FenceGateBlock(SHAPE_FRAME_SETTINGS))
    .hasItem(Item.Settings())
    .renderLayer(RenderLayer.getCutout())
    .done()

@Suppress("deprecation")
val HOLLOW_FRAME_ID = SpriteIdentifier(
    SpriteAtlasTexture.BLOCK_ATLAS_TEX,
    Identifier("framity", "block/hollow_frame")
)

val BLOCK_FRAME = MOD.block("block_frame", BlockFrame())
    .hasItem(Item.Settings(), "framity")
    .renderLayer(RenderLayer.getCutout())
    .modelsFrom(SHAPE_BLOCK_FRAME, listOf(HOLLOW_FRAME_ID))
    .done()
val BLOCK_FRAME_ENTITY = MOD.blockEntity("block_frame_entity", ::FrameEntity, BLOCK_FRAME)

val SLAB_FRAME = MOD.block("slab_frame", SlabFrame())
    .hasItem(Item.Settings(), "framity")
    .renderLayer(RenderLayer.getCutout())
    .modelsFrom(SHAPE_SLAB_FRAME, listOf(HOLLOW_FRAME_ID))
    .done()
val SLAB_FRAME_ENTITY = MOD.blockEntity("slab_frame_entity", ::FrameEntity, SLAB_FRAME)

val STAIRS_FRAME = MOD.block("stairs_frame", StairsFrame())
    .hasItem(Item.Settings(), "framity")
    .renderLayer(RenderLayer.getCutout())
    .modelsFrom(SHAPE_STAIRS_FRAME, listOf(HOLLOW_FRAME_ID))
    .done()
val STAIRS_FRAME_ENTITY = MOD.blockEntity("stairs_frame_entity", ::FrameEntity, STAIRS_FRAME)

val FENCE_FRAME = MOD.block("fence_frame", FenceFrame())
    .hasItem(Item.Settings(), "framity")
    .renderLayer(RenderLayer.getCutout())
    .modelsFrom(SHAPE_FENCE_FRAME, listOf(HOLLOW_FRAME_ID))
    .done()
val FENCE_FRAME_ENTITY = MOD.blockEntity("fence_frame_entity", ::FrameEntity, FENCE_FRAME)

val FENCE_GATE_FRAME = MOD.block("fence_gate_frame", FenceGateFrame())
    .hasItem(Item.Settings(), "framity")
    .renderLayer(RenderLayer.getCutout())
    .modelsFrom(SHAPE_FENCE_GATE_FRAME, listOf(HOLLOW_FRAME_ID))
    .done()
val FENCE_GATE_FRAME_ENTITY = MOD.blockEntity("fence_gate_frame_entity", ::FrameEntity, FENCE_GATE_FRAME)

lateinit var FRAME_SCREEN_HANDLER_TYPE: ScreenHandlerType<FrameGuiDescription>

@Suppress("unused")
fun init() {
    MOD.register()

    FRAME_SCREEN_HANDLER_TYPE = ScreenHandlerRegistry.registerSimple(MOD.id("frame_screen_handler"), FrameGuiDescription)

    ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(FramityDataListener())
}

@Suppress("unused")
fun clientInit() {
    MOD.registerClient()

    ScreenRegistry.register(FRAME_SCREEN_HANDLER_TYPE, FrameScreen)

    @Suppress("deprecation")
    ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEX).register(ClientSpriteRegistryCallback { _, registry -> with(registry) {
        register(MOD.id("block/snow_side_overlay"))
        register(MOD.id("block/mycelium_side_overlay"))
        register(MOD.id("block/hay_side_overlay"))
        register(MOD.id("block/path_side_overlay"))
    }})

    ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(FramityAssetsListener())
}
