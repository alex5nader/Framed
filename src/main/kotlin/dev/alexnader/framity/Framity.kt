package dev.alexnader.framity

import dev.alexnader.framity.util.WithId
import dev.alexnader.framity.block_entities.FrameEntity
import dev.alexnader.framity.blocks.*
import dev.alexnader.framity.items.FramersHammer
import dev.alexnader.framity.mixin.AccessibleStairsBlock
import dev.alexnader.framity.model.v2.FramityModelVariantProvider
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.*
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

val MOD = Mod("framity", FramityModelVariantProvider())
    .itemGroup("framity") { ItemStack(FRAMERS_HAMMER.value) }
    .done()

@Suppress("unused")
val UV_TEST = MOD.block("uv_test", Block(FabricBlockSettings.of(Material.STONE)))

@JvmField
val FRAMERS_HAMMER: WithId<Item> = MOD.item("framers_hammer", FramersHammer())
    .itemGroup("framity")
    .done()

val SHAPE_BLOCK_FRAME = MOD.block("shape_block_frame", Block(SHAPE_FRAME_SETTINGS))
    .hasItem(Item.Settings())
    .done()
val SHAPE_SLAB_FRAME = MOD.block("shape_slab_frame", SlabBlock(SHAPE_FRAME_SETTINGS))
    .hasItem(Item.Settings())
    .done()
val SHAPE_STAIRS_FRAME = MOD.block("shape_stairs_frame", AccessibleStairsBlock.create(SHAPE_BLOCK_FRAME.value.defaultState, SHAPE_FRAME_SETTINGS))
    .hasItem(Item.Settings())
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

@Suppress("unused")
fun init() {
    MOD.register()
}

@Suppress("unused")
fun clientInit() {
    MOD.registerClient()
}
