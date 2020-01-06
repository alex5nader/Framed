package dev.alexnader.framity.adapters

import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.BlockItem
import net.minecraft.item.Item

data class KtItem<out I: Item>(val item: I, val id: String)

data class KtBlock<out B: Block>(val block: B, val id: String) {
    var blockItem: BlockItem? = null
}

data class KtBlockEntity<E: BlockEntity>(val blockEntity: BlockEntityType<E>, val id: String)
