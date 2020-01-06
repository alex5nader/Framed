package dev.alexnader.framity.block_entities

import com.mojang.datafixers.Dynamic
import dev.alexnader.framity.adapters.KtBlock
import dev.alexnader.framity.adapters.KtBlockEntity
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity
import net.fabricmc.fabric.api.server.PlayerStream
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.datafixers.NbtOps
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.DefaultedList
import net.minecraft.util.math.Direction

class FrameEntity<B: Block>(
    private val ktBlock: KtBlock<B>,
    type: KtBlockEntity<FrameEntity<B>>
): InventoryBlockEntity(
    type.blockEntity, DefaultedList.ofSize(2, ItemStack.EMPTY)
), RenderAttachmentBlockEntity {
    companion object {
        const val ContainedSlot = 0
        const val GlowstoneSlot = 1
    }

    var containedState: BlockState? = null
        set(v) {
            field = v
            this.markDirty()
        }

    var containedStack
        get() = this[ContainedSlot]
        set(stack) {
            this[ContainedSlot] = stack
        }
    val item: Item get() = this.containedStack.item

    var glowstoneStack
        get() = this[GlowstoneSlot]
        set(stack) {
            this[GlowstoneSlot] = stack
        }

    val highestRemovePrioritySlot get() = this.items.indices.findLast { !this.items[it].isEmpty } ?: -1

    var highestRemovePriority
        get() = this[this.highestRemovePrioritySlot]
        set(stack) {
            this[this.highestRemovePrioritySlot] = stack
        }

    override fun getRenderAttachmentData() = this.containedState

    override fun fromTag(tag: CompoundTag?) {
        super.fromTag(tag)
        if (tag?.contains("state") == true) {
            this.containedState = BlockState.deserialize(Dynamic(NbtOps.INSTANCE, tag.get("state")))
        } else {
            this.containedState = null
        }
    }

    override fun markDirty() {
        super.markDirty()

        if (this.world?.isClient == false) {
            for (obj in PlayerStream.watching(this)) {
                (obj as ServerPlayerEntity).networkHandler.sendPacket(this.toUpdatePacket())
            }
            this.world!!.updateNeighborsAlways(pos.offset(Direction.UP), this.ktBlock.block)
            val state = this.world!!.getBlockState(pos)
            this.world!!.updateListeners(pos, state, state, 1)
        }
        if (this.world?.isClient == true) {
            MinecraftClient.getInstance().worldRenderer.updateBlock(this.world, this.pos, this.ktBlock.block.defaultState, this.ktBlock.block.defaultState, 1)
        }
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        if (this.containedState == null) {
            tag?.remove("state")
        } else {
            tag?.put("state", BlockState.serialize(NbtOps.INSTANCE, this.containedState).value)
        }
        return super.toTag(tag)
    }
}
