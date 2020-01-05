package dev.alexnader.framity

import dev.alexnader.framity.adapters.KtBlock
import dev.alexnader.framity.block_entities.FrameEntity
import dev.alexnader.framity.blocks.*
import dev.alexnader.framity.model.*
import dev.alexnader.framity.util.curried
import dev.alexnader.framity.util.uncurried
import grondag.fermion.client.models.AbstractModel
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.renderer.v1.Renderer
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.HorizontalConnectedBlock
import net.minecraft.block.StairsBlock
import net.minecraft.client.texture.Sprite
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.state.property.Properties
import net.minecraft.state.property.Property
import net.minecraft.util.Identifier
import java.util.function.Function

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

@Suppress("deprecation")
val HOLLOW_FRAME_ID = SpriteIdentifier(
    SpriteAtlasTexture.BLOCK_ATLAS_TEX,
    Identifier("framity", "block/hollow_frame")
)
@Suppress("deprecation")
val FULL_FRAME_ID = SpriteIdentifier(
    SpriteAtlasTexture.BLOCK_ATLAS_TEX,
    Identifier("framity", "block/full_frame")
)
@Suppress("deprecation")
val UV_ID = SpriteIdentifier(
    SpriteAtlasTexture.BLOCK_ATLAS_TEX,
    Identifier("framity", "block/uv")
)

val MODEL_VARIANT_PROVIDER = FramityModelVariantProvider()

@Suppress("unused")
fun init() {
    MOD.creativeTab(BLOCK_FRAME)

    MOD.registerAll()
}

fun <B: Block> registerModel(
    ktBlock: KtBlock<B>,
    sprites: List<SpriteIdentifier>,
    properties: List<Property<out Comparable<*>>>,
    model: (Block) -> (List<Property<out Comparable<*>>>) -> (SpriteIdentifier) -> (() -> MeshTransformer) -> (BlockState) -> (Function<SpriteIdentifier, Sprite>) -> AbstractModel
) {
    MODEL_VARIANT_PROVIDER.registerModels(
        ktBlock.block,
        ktBlock.block.defaultState,
        model(ktBlock.block)(properties)(sprites[0])(VoxelTransformer.ofSprite(sprites[0])).uncurried(),
        sprites
    )
}

@Suppress("unused")
fun clientInit() {
    MOD.registerAllClient()

    ModelLoadingRegistry.INSTANCE.registerVariantProvider { MODEL_VARIANT_PROVIDER }
    registerModel(
        BLOCK_FRAME,
        listOf(HOLLOW_FRAME_ID),
        emptyList(),
        (::FramityVoxelModel).curried()
    )
    registerModel(
        SLAB_FRAME,
        listOf(HOLLOW_FRAME_ID),
        listOf(Properties.FACING),
        (::FramityVoxelModel).curried()
    )
    registerModel(
        STAIRS_FRAME,
        listOf(HOLLOW_FRAME_ID),
        listOf(StairsBlock.FACING, StairsBlock.HALF, StairsBlock.SHAPE),
        (::FramityVoxelModel).curried()
    )
    registerModel(
        FENCE_FRAME,
        listOf(FULL_FRAME_ID),
        listOf(HorizontalConnectedBlock.NORTH, HorizontalConnectedBlock.EAST, HorizontalConnectedBlock.SOUTH, HorizontalConnectedBlock.WEST),
        (::CustomItemFramityVoxelModel).curried()((FenceFrame)::getItemMesh)
    )
}
