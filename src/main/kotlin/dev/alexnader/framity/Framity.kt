package dev.alexnader.framity

import dev.alexnader.framity.adapters.KtBlock
//import dev.alexnader.framity.block_entities.BlockFrameEntity
import dev.alexnader.framity.block_entities.FrameEntity
//import dev.alexnader.framity.block_entities.SlabFrameEntity
//import dev.alexnader.framity.block_entities.StairsFrameEntity
import dev.alexnader.framity.blocks.*
import dev.alexnader.framity.model.FramityModelVariantProvider
import dev.alexnader.framity.model.FramityVoxelModel
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.HorizontalConnectedBlock
import net.minecraft.block.StairsBlock
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.state.property.Properties
import net.minecraft.state.property.Property
import net.minecraft.util.Identifier

val MOD = Mod("framity")

//val SIMPLE_BLOCK_FRAME = MOD.block(::SimpleBlockFrame, "simple_block_frame")
//val SIMPLE_SLAB_FRAME = MOD.block(::SimpleSlabFrame, "simple_slab_frame")
//val SIMPLE_STAIRS_FRAME = MOD.block(::SimpleStairsFrame, "simple_stairs_frame")

val BLOCK_FRAME = MOD.block(::BlockFrame, "block_frame")
val BLOCK_FRAME_ENTITY = MOD.blockEntity(::FrameEntity, "block_frame_entity", BLOCK_FRAME)

val SLAB_FRAME = MOD.block(::SlabFrame, "slab_frame")
val SLAB_FRAME_ENTITY = MOD.blockEntity(::FrameEntity, "slab_frame_entity", SLAB_FRAME)

val STAIRS_FRAME = MOD.block(::StairsFrame, "stairs_frame")
val STAIRS_FRAME_ENTITY = MOD.blockEntity(::FrameEntity, "stairs_frame_entity", STAIRS_FRAME)

val FENCE_FRAME = MOD.block(::FenceFrame, "fence_frame")
val FENCE_FRAME_ENTITY = MOD.blockEntity(::FrameEntity, "fence_frame_entity", FENCE_FRAME)

val FRAME_SPRITE_IDENTIFIER = SpriteIdentifier(
    SpriteAtlasTexture.BLOCK_ATLAS_TEX,
    Identifier("framity", "block/frame")
)
val UV_SPRITE_IDENTIFIER = SpriteIdentifier(
    SpriteAtlasTexture.BLOCK_ATLAS_TEX,
    Identifier("framity", "block/uv")
)

val MODEL_VARIANT_PROVIDER = FramityModelVariantProvider()

@Suppress("unused")
fun init() {
    MOD.creativeTab(BLOCK_FRAME)

    MOD.registerAll()
}

fun <B: Block> registerVoxelModel(
    ktBlock: KtBlock<B>,
    sprites: List<SpriteIdentifier>,
    properties: List<Property<out Comparable<*>>>,
    defaultState: (BlockState) -> BlockState = { it }
) {
    MODEL_VARIANT_PROVIDER.registerModels(
        ktBlock.block,
        defaultState(ktBlock.block.defaultState),
        FramityVoxelModel.of(ktBlock.block, properties),
        sprites
    )
}

@Suppress("unused")
fun clientInit() {
    MOD.registerAllClient()

    ModelLoadingRegistry.INSTANCE.registerVariantProvider { MODEL_VARIANT_PROVIDER }
    registerVoxelModel(BLOCK_FRAME, listOf(FRAME_SPRITE_IDENTIFIER), emptyList())
    registerVoxelModel(SLAB_FRAME, listOf(FRAME_SPRITE_IDENTIFIER), listOf(Properties.FACING))
    registerVoxelModel(STAIRS_FRAME, listOf(FRAME_SPRITE_IDENTIFIER), listOf(StairsBlock.FACING, StairsBlock.HALF, StairsBlock.SHAPE))
    registerVoxelModel(FENCE_FRAME, listOf(FRAME_SPRITE_IDENTIFIER), listOf(HorizontalConnectedBlock.NORTH, HorizontalConnectedBlock.EAST, HorizontalConnectedBlock.SOUTH, HorizontalConnectedBlock.WEST))
}
