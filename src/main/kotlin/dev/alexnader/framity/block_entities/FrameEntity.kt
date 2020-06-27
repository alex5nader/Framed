package dev.alexnader.framity.block_entities

import com.mojang.serialization.Dynamic
import dev.alexnader.framity.util.WithId
import dev.alexnader.framity.data.getOverlayId
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity
import net.fabricmc.fabric.api.server.PlayerStream
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.MinecraftClient
import net.minecraft.datafixer.NbtOps
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.Direction

/**
 * Block entity for all frames.
 *
 * @param ktBlock The frame to use.
 * @param type The block entity type to use.
 */
class FrameEntity<B: Block>(
    private val ktBlock: WithId<B>,
    type: WithId<BlockEntityType<FrameEntity<B>>>
): InventoryBlockEntity(
    type.value, DefaultedList.ofSize(3, ItemStack.EMPTY)
), RenderAttachmentBlockEntity, BlockEntityClientSerializable {
    companion object {
        /**
         * [Inventory][net.minecraft.inventory.Inventory] slot for the contained block.
         */
        const val ContainedSlot = 0
        /**
         * [Inventory][net.minecraft.inventory.Inventory] slot for glowstone dust.
         */
        const val GlowstoneSlot = 1
        /**
         * [Inventory][net.minecraft.inventory.Inventory] slot for overlay.
         */
        const val OverlaySlot = 2
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
     * The [ItemStack] for the contained overlay.
     */
    var overlayStack
        get() = this[OverlaySlot]
        set(stack) {
            this[OverlaySlot] = stack
        }

    /**
     * The index of the "rightmost" item in this frame which isn't empty.
     */
    val highestRemovePrioritySlot get() = this.items.indices.findLast { !this.items[it].isEmpty } ?: -1

    /**
     * [RenderAttachmentBlockEntity] implementation returning the contained [BlockState].
     */
    override fun getRenderAttachmentData() =
        Pair(this.containedState, getOverlayId(this.overlayStack))

    /**
     * Marks this frame as dirty. Causes client to re-render the block when called.
     */
    override fun markDirty() {
        super.markDirty()

        if (this.world?.isClient == false) {
            for (obj in PlayerStream.watching(this)) {
                (obj as ServerPlayerEntity).networkHandler.sendPacket(this.toUpdatePacket())
            }
            this.world!!.updateNeighborsAlways(pos.offset(Direction.UP), this.ktBlock.value)
            val state = this.world!!.getBlockState(pos)
            this.world!!.updateListeners(pos, state, state, 1)
        }
        if (this.world?.isClient == true) {
            MinecraftClient.getInstance().worldRenderer.updateBlock(this.world, this.pos, this.ktBlock.value.defaultState, this.ktBlock.value.defaultState, 1)
        }
    }

    /**
     * Reads this frame's data from [tag].
     */
    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        super.fromTag(state, tag)
        this.fromClientTag(tag)
    }

    /**
     * Writes this frame's data to [tag].
     */
    override fun toTag(tag: CompoundTag?): CompoundTag {
        val tag2 = this.toClientTag(tag)
        return super.toTag(tag2)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        super.fromClientTag(tag)
        if (tag?.contains("state") == true) {
            this.containedState = BlockState.CODEC.decode(Dynamic(NbtOps.INSTANCE, tag.get("state"))).result().get().first
        } else {
            this.containedState = null
        }
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        if (this.containedState == null) {
            tag?.remove("state")
        } else {
            tag?.put("state", BlockState.CODEC.encode(this.containedState, NbtOps.INSTANCE, CompoundTag()).get().left().get())
        }
        return super.toClientTag(tag)
    }
}
