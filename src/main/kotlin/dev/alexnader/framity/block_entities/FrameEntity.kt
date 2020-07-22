package dev.alexnader.framity.block_entities

import dev.alexnader.framity.FRAME_ENTITY
import dev.alexnader.framity.SPECIAL_ITEMS
import dev.alexnader.framity.blocks.validForBase
import dev.alexnader.framity.blocks.validForSpecial
import dev.alexnader.framity.blocks.validForOverlay
import dev.alexnader.framity.client.gui.FrameGuiDescription
import dev.alexnader.framity.data.getOverlayId
import dev.alexnader.framity.mixin.GetItemBeforeEmpty
import dev.alexnader.framity.util.*
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.fabricmc.fabric.api.server.PlayerStream
import net.minecraft.block.BlockState
import net.minecraft.block.entity.LockableContainerBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.TranslatableText
import net.minecraft.util.math.Direction
import kotlin.math.max
import kotlin.math.min

open class FrameEntity constructor(format: FrameDataFormat) :
    LockableContainerBlockEntity(FRAME_ENTITY.value),
    ExtendedScreenHandlerFactory,
    RenderAttachmentBlockEntity,
    BlockEntityClientSerializable
{
    companion object {
        val FORMAT = FrameDataFormat(1)
    }

    constructor() : this(FORMAT)

    val data = FrameData(format, SectionedList(format, ItemStackEquality) { ItemStack.EMPTY }, FixedSizeList(format.base.size) { null })

    val format get() = data.format

    val items get() = data.items

    val baseItems get() = items.getSection(FrameDataFormat.BASE_INDEX)
    val overlayItems get() = items.getSection(FrameDataFormat.OVERLAY_INDEX)
    val specialItems get() = items.getSection(FrameDataFormat.SPECIAL_INDEX)

    val baseStates get() = data.baseStates

    //region Inventory

    fun copyFrom(slot: Int, stack: ItemStack, count: Int, take: Boolean) {
        val newStack = stack.copy()
        val realCount = min(count, stack.count)

        newStack.count = realCount

        if (take) {
            stack.count -= realCount
        }

        this.setStack(slot, newStack)
    }

    // interface
    override fun getMaxCountPerStack() = 1

    override fun isValid(slot: Int, stack: ItemStack) =
        when (format.getSectionIndex(slot)) {
            FrameDataFormat.BASE_INDEX -> validForBase(stack, { s -> s.block.defaultState }, this.world!!, this.pos) != null
            FrameDataFormat.OVERLAY_INDEX -> validForOverlay(stack)
            FrameDataFormat.SPECIAL_INDEX -> validForSpecial(stack)
            else -> false
        }

    private fun beforeRemove(slot: Int) {
        val sectionIndex = format.getSectionIndex(slot)

        if (sectionIndex == FrameDataFormat.BASE_INDEX) {
            this.baseStates.removeAt(format.base.findOffset(slot))
        } else if (sectionIndex == FrameDataFormat.SPECIAL_INDEX) {
            @Suppress("CAST_NEVER_SUCCEEDS")
            val existing = this.getStack(slot) as GetItemBeforeEmpty
            SPECIAL_ITEMS[existing.itemBeforeEmpty]?.data?.onRemove(this.world!!, this)
        }
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack {
        beforeRemove(slot)
        val result =
            if (slot >= 0 && slot < items.size && !items[slot].isEmpty && amount > 0) {
                items[slot].split(amount).also {
                    if (items.updateEmptyAt(slot)) {
                        markDirty()
                    }
                }
            } else {
                ItemStack.EMPTY
            }

        if (!result.isEmpty) {
            this.markDirty()
        }

        return result
    }

    override fun removeStack(slot: Int): ItemStack {
        beforeRemove(slot)

        this.markDirty()

        return if (slot >= 0 && slot < items.size)
            items.set(slot, ItemStack.EMPTY)
        else
            ItemStack.EMPTY
    }

    override fun getStack(slot: Int) = this.items[slot]

    override fun size() = this.items.size

    override fun isEmpty() = this.items.isEmpty()

    override fun canPlayerUse(player: PlayerEntity) = true

    override fun clear() {
        SPECIAL_ITEMS.forEach { (_, specialItem) -> specialItem.data.onRemove(this.world!!, this) }
        this.items.clear()
    }

    override fun setStack(slot: Int, stack: ItemStack) {
        val sectionIndex = format.getSectionIndex(slot)

        if (sectionIndex == FrameDataFormat.SPECIAL_INDEX) {
            @Suppress("CAST_NEVER_SUCCEEDS")
            val existing = this.getStack(slot) as GetItemBeforeEmpty
            SPECIAL_ITEMS[existing.itemBeforeEmpty]?.data?.onRemove(this.world!!, this)
        }

        this.items[slot] = stack
        stack.count = max(stack.count, this.maxCountPerStack)
        this.markDirty()

        if (sectionIndex == FrameDataFormat.BASE_INDEX) {
            val baseSlot = format.base.findOffset(slot)
            this.baseStates[baseSlot] =
                (this.baseItems[baseSlot].item as? BlockItem)?.block?.defaultState
        } else if (sectionIndex == FrameDataFormat.SPECIAL_INDEX) {
            SPECIAL_ITEMS[stack.item]?.data?.onAdd(this.world!!, this)
        }
    }

    override fun markDirty() {
        super.markDirty()

        val world = this.world ?: return
        val state = world.getBlockState(this.pos)
        val block = state.block

        if (world.isClient) {
            MinecraftClient.getInstance().worldRenderer.updateBlock(world, pos, cachedState, state, 1)
        } else {
            sync()

            for (obj in PlayerStream.watching(this)) {
                (obj as ServerPlayerEntity).networkHandler.sendPacket(this.toUpdatePacket())
            }

            world.updateNeighborsAlways(pos.offset(Direction.UP), block)
        }
    }
    //endregion Inventory

    //region RenderAttachmentBlockEntity
    override fun getRenderAttachmentData() =
        this.items.getSectionOrNull(FrameDataFormat.OVERLAY_INDEX)?.let {
            Pair(this.baseStates, this.overlayItems.map(::getOverlayId))
        }
    //endregion RenderAttachmentBlockEntity

    //region Tag
    override fun toTag(tag: CompoundTag): CompoundTag {
        toClientTag(tag)
        return super.toTag(tag)
    }

    override fun fromTag(state: BlockState, tag: CompoundTag) {
        fromClientTag(tag)
        super.fromTag(state, tag)
    }

    //region BlockEntityClientSerializable
    override fun toClientTag(tag: CompoundTag) = tag.also {
        tag.put("frameData", this.data.toTag())
    }

    override fun fromClientTag(tag: CompoundTag) {
        this.data.fromTag(tag.getCompound("frameData"))
        this.markDirty()
    }
    //endregion BlockEntityClientSerializable

    //endregion Tag

    //region ExtendedScreenHandlerFactory
    override fun createScreenHandler(syncId: Int, playerInventory: PlayerInventory?) =
        FrameGuiDescription(syncId, playerInventory, ScreenHandlerContext.create(this.world, this.pos))

    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        buf.writeBlockPos(this.pos)
    }
    //endregion ExtendedScreenHandlerFactory

    override fun getContainerName() = TranslatableText(cachedState.block.translationKey)
}
