package dev.alexnader.framity.util

import com.mojang.serialization.Dynamic
import dev.alexnader.framity.block_entities.FrameEntity
import net.minecraft.block.BlockState
import net.minecraft.datafixer.NbtOps
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.collection.DefaultedList

data class FrameData(val items: DefaultedList<ItemStack>, var baseState: BlockState? = null) {
    companion object {
        fun fromTag(tag: CompoundTag) =
            FrameData(
                DefaultedList.ofSize(FrameEntity.SLOT_COUNT, ItemStack.EMPTY)
                    .apply { Inventories.fromTag(tag, this) },
                if (tag.contains("state")) {
                    BlockState.CODEC.decode(Dynamic(NbtOps.INSTANCE, tag.get("state"))).result().get().first
                } else {
                    null
                }
            )
    }

    fun toTag() =
        CompoundTag().apply {
            Inventories.toTag(this, this@FrameData.items)
            this@FrameData.baseState?.let { this.put("state",
                BlockState.CODEC.encode(it, NbtOps.INSTANCE, CompoundTag()).get().left().get())
            }
        }
}