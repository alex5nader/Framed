package dev.alexnader.framity

import com.google.gson.Gson
import dev.alexnader.framity.block_entities.FrameEntity
import dev.alexnader.framity.util.WithId
import dev.alexnader.framity.blocks.*
import dev.alexnader.framity.client.assets.FramityAssetsListener
import dev.alexnader.framity.data.FramityDataListener
import dev.alexnader.framity.client.gui.FrameGuiDescription
import dev.alexnader.framity.client.gui.FrameScreen
import dev.alexnader.framity.items.FramersHammer
import dev.alexnader.framity.client.model.FramityModelVariantProvider
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.`object`.builder.v1.client.model.FabricModelPredicateProviderRegistry
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry
import net.minecraft.block.*
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.resource.ResourceType
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.ScreenHandlerType
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

val GSON = Gson()

val LOGGER: Logger = LogManager.getLogger("Framity")

@JvmField
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
val SHAPE_TRAPDOOR_FRAME = MOD.block("shape_trapdoor_frame", Trapdoor(SHAPE_FRAME_SETTINGS))
    .hasItem(Item.Settings())
    .renderLayer(RenderLayer.getCutout())
    .done()

@Suppress("deprecation")
val HOLLOW_FRAME_ID = SpriteIdentifier(
    SpriteAtlasTexture.BLOCK_ATLAS_TEX,
    MOD.id("block/hollow_frame")
)
@Suppress("deprecation")
val SOLID_FRAME_ID = SpriteIdentifier(
    SpriteAtlasTexture.BLOCK_ATLAS_TEX,
    MOD.id("block/solid_frame")
)

val BLOCK_FRAME = MOD.block("block_frame", BlockFrame())
    .hasItem(Item.Settings(), "framity")
    .renderLayer(RenderLayer.getCutout())
    .hasDelegateModel(SHAPE_BLOCK_FRAME, listOf(HOLLOW_FRAME_ID), FrameEntity.FORMAT.partCount)
    .done()
val SLAB_FRAME = MOD.block("slab_frame", SlabFrame())
    .hasItem(Item.Settings(), "framity")
    .renderLayer(RenderLayer.getCutout())
    .hasDelegateModel(SHAPE_SLAB_FRAME, listOf(HOLLOW_FRAME_ID), SlabFrame.FORMAT.partCount)
    .done()
val STAIRS_FRAME = MOD.block("stairs_frame", StairsFrame())
    .hasItem(Item.Settings(), "framity")
    .renderLayer(RenderLayer.getCutout())
    .hasDelegateModel(SHAPE_STAIRS_FRAME, listOf(HOLLOW_FRAME_ID), FrameEntity.FORMAT.partCount)
    .done()
val FENCE_FRAME = MOD.block("fence_frame", FenceFrame())
    .hasItem(Item.Settings(), "framity")
    .renderLayer(RenderLayer.getCutout())
    .hasDelegateModel(SHAPE_FENCE_FRAME, listOf(SOLID_FRAME_ID), FrameEntity.FORMAT.partCount)
    .done()
val FENCE_GATE_FRAME = MOD.block("fence_gate_frame", FenceGateFrame())
    .hasItem(Item.Settings(), "framity")
    .renderLayer(RenderLayer.getCutout())
    .hasDelegateModel(SHAPE_FENCE_GATE_FRAME, listOf(SOLID_FRAME_ID), FrameEntity.FORMAT.partCount)
    .done()
val TRAPDOOR_FRAME = MOD.block("trapdoor_frame", TrapdoorFrame())
    .hasItem(Item.Settings(), "framity")
    .renderLayer(RenderLayer.getCutout())
    .hasDelegateModel(SHAPE_TRAPDOOR_FRAME, listOf(SOLID_FRAME_ID), FrameEntity.FORMAT.partCount)
    .done()

val FRAME_ENTITY = MOD.blockEntity("frame_entity", ::FrameEntity,
    BLOCK_FRAME.value,
    SLAB_FRAME.value,
    STAIRS_FRAME.value,
    FENCE_FRAME.value,
    FENCE_GATE_FRAME.value,
    TRAPDOOR_FRAME.value
)

lateinit var FRAME_SCREEN_HANDLER_TYPE: ScreenHandlerType<FrameGuiDescription>

@Suppress("unused")
fun init() {
    MOD.register()

    FRAME_SCREEN_HANDLER_TYPE = ScreenHandlerRegistry.registerExtended(MOD.id("frame_screen_handler")) { syncId, inventory, buf ->
        FrameGuiDescription(syncId, inventory, ScreenHandlerContext.create(inventory.player.world, buf.readBlockPos()))
    }

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

    ModelLoadingRegistry.INSTANCE.registerAppender { _, out ->
        out.accept(ModelIdentifier(MOD.id("framers_hammer_none"), "inventory"))
    }

    FabricModelPredicateProviderRegistry.register(FRAMERS_HAMMER.value, MOD.id("hammer_mode"), FramersHammer.Companion.ModelPredicate)
}
