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

/**
 * Block entity for all frames.
 *
 * @param ktBlock The frame to use.
 * @param type The block entity type to use.
 */
class FrameEntity<B: Block>(
    private val ktBlock: KtBlock<B>,
    type: KtBlockEntity<FrameEntity<B>>
): InventoryBlockEntity(
    type.blockEntity, DefaultedList.ofSize(2, ItemStack.EMPTY)
), RenderAttachmentBlockEntity {
    companion object {
        /**
         * [Inventory][net.minecraft.inventory.Inventory] slot for the contained block.
         */
        const val ContainedSlot = 0
        /**
         * [Inventory][net.minecraft.inventory.Inventory] slot for glowstone dust.
         */
        const val GlowstoneSlot = 1
    }

    /**
     * The contained [BlockState].
     */
    var containedState: BlockState? = null
        set(v) {
            field = v
            this.markDirty()
        }

    /**
     * The [ItemStack] for the contained block.
     */
    var containedStack
        get() = this[ContainedSlot]
        set(stack) {
            this[ContainedSlot] = stack
        }
    /**
     * The [Item] for the contained block.
     */
    val item: Item get() = this.containedStack.item

    /**
     * The [ItemStack] for the contained glowstone dust.
     */
    var glowstoneStack
        get() = this[GlowstoneSlot]
        set(stack) {
            this[GlowstoneSlot] = stack
        }

    /**
     * The index of the "rightmost" item in this frame which isn't empty.
     */
    val highestRemovePrioritySlot get() = this.items.indices.findLast { !this.items[it].isEmpty } ?: -1

    /**
     * The "rightmost" [ItemStack] in this frame which isn't empty.
     */
    var highestRemovePriority
        get() = this[this.highestRemovePrioritySlot]
        set(stack) {
            this[this.highestRemovePrioritySlot] = stack
        }

    /**
     * [RenderAttachmentBlockEntity] implementation returning the contained [BlockState].
     */
    override fun getRenderAttachmentData() = this.containedState

    /**
     * Reads this frame's data from [tag].
     */
    override fun fromTag(tag: CompoundTag?) {
        super.fromTag(tag)
        if (tag?.contains("state") == true) {
            this.containedState = BlockState.deserialize(Dynamic(NbtOps.INSTANCE, tag.get("state")))
        } else {
            this.containedState = null
        }
    }

    /**
     * Marks this frame as dirty. Causes client to re-render the block when called.
     */
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

    /**
     * Writes this frame's data to [tag].
     */
    override fun toTag(tag: CompoundTag?): CompoundTag {
        if (this.containedState == null) {
            tag?.remove("state")
        } else {
            tag?.put("state", BlockState.serialize(NbtOps.INSTANCE, this.containedState).value)
        }
        return super.toTag(tag)
    }
}
