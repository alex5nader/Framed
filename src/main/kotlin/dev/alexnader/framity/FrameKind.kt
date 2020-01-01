package dev.alexnader.framity

import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.StringIdentifiable

@Suppress("UNCHECKED_CAST")
enum class FrameKind: StringIdentifiable {
    WholeBlock {
        override fun asString() = "whole_block"
        override fun <B: Block> getBlock() = BLOCK_FRAME.block as B
        override fun <E: BlockEntity> getBlockEntity() = BLOCK_FRAME_ENTITY.blockEntity as BlockEntityType<E>
    },
    Slab {
        override fun asString() = "slab"
        override fun <B: Block> getBlock() = SLAB_FRAME.block as B
        override fun <E: BlockEntity> getBlockEntity() = SLAB_FRAME_ENTITY.blockEntity as BlockEntityType<E>
    },
    Stairs {
        override fun asString() = "stair"
        override fun <B: Block> getBlock() = STAIRS_FRAME.block as B
        override fun <E: BlockEntity> getBlockEntity() = STAIRS_FRAME_ENTITY.blockEntity as BlockEntityType<E>
    },
    Slope {
        override fun asString() = "slope"
        override fun <B: Block> getBlock() = TODO("Slope not implemented")
        override fun <E: BlockEntity> getBlockEntity() = TODO("Slope not implemented")
    }, ;

    abstract fun <B: Block> getBlock(): B
    abstract fun <E: BlockEntity> getBlockEntity(): BlockEntityType<E>

    companion object {
        fun fromString(kind: String) = when (kind) {
            "whole_block" -> WholeBlock
            "slab" -> Slab
            "stair" -> Stairs
            "slope" -> Slope
            else -> null
        }
    }
}