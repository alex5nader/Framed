package dev.alexnader.framity.block_entities

import com.mojang.serialization.Dynamic
import dev.alexnader.framity.data.getOverlayId
import dev.alexnader.framity.util.*
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity
import net.fabricmc.fabric.api.server.PlayerStream
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.MinecraftClient
import net.minecraft.datafixer.NbtOps
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.state.property.BooleanProperty
import net.minecraft.text.TranslatableText
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.Direction
import net.minecraft.world.World

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
    type.value, DefaultedList.ofSize(SLOT_COUNT, ItemStack.EMPTY)
), RenderAttachmentBlockEntity, BlockEntityClientSerializable {
    companion object {
        /**
         * [Inventory][net.minecraft.inventory.Inventory] slot for the base block.
         */
        const val BASE_SLOT = 0
        /**
         * [Inventory][net.minecraft.inventory.Inventory] slot for overlay.
         */
        const val OVERLAY_SLOT = 1

        /**
         * [Inventory][net.minecraft.inventory.Inventory] slot for glowstone dust.
         */
        const val OTHER_SLOTS_START = 2

        sealed class OtherItem(val offset: Int) {
            class BindsProperty(offset: Int, private val property: BooleanProperty) : OtherItem(offset) {
                override fun onAdd(world: World, frameEntity: FrameEntity<*>) {
                    println("Setting $property to true")
                    world.setBlockState(frameEntity.pos, frameEntity.cachedState.with(property, true))
                }

                override fun onRemove(world: World, frameEntity: FrameEntity<*>) {
                    println("Setting $property to false")
                    world.setBlockState(frameEntity.pos, frameEntity.cachedState.with(property, false))
                }
            }

            class Many(offset: Int, private vararg val others: OtherItem) : OtherItem(offset) {
                override fun onAdd(world: World, frameEntity: FrameEntity<*>) {
                    this.others.forEach { it.onAdd(world, frameEntity) }
                }

                override fun onRemove(world: World, frameEntity: FrameEntity<*>) {
                    this.others.forEach { it.onRemove(world, frameEntity) }
                }
            }

            class Dummy(offset: Int) : OtherItem(offset) {
                override fun onAdd(world: World, frameEntity: FrameEntity<*>) {}
                override fun onRemove(world: World, frameEntity: FrameEntity<*>) {}
            }

            val slot get() = OTHER_SLOTS_START + this.offset

            abstract fun onAdd(world: World, frameEntity: FrameEntity<*>)
            abstract fun onRemove(world: World, frameEntity: FrameEntity<*>)
        }

        @JvmField
        val OTHER_ITEM_DATA = mapOf(
            Items.GLOWSTONE_DUST to OtherItem.BindsProperty(0, HasGlowstone),
            Items.REDSTONE to OtherItem.Dummy(1),
            Items.APPLE to OtherItem.Dummy(2),
            Items.WHEAT to OtherItem.Dummy(3)
        )

        val SLOT_COUNT = 2 + OTHER_ITEM_DATA.size
    }

    /**
     * The base [BlockState].
     */
    var baseState: BlockState? = null
        set(v) {
            field = v
            this.markDirty()
        }

    val baseStack
        get() = this[BASE_SLOT]

    val glowstoneStack
        get() = this[OTHER_SLOTS_START]

    val overlayStack
        get() = this[OVERLAY_SLOT]

    /**
     * The index of the "rightmost" item in this frame which isn't empty.
     */
    val highestRemovePrioritySlot get() = this.items.indices.findLast { !this.items[it].isEmpty } ?: -1

    /**
     * [RenderAttachmentBlockEntity] implementation returning the base [BlockState] and the overlay ID.
     */
    override fun getRenderAttachmentData() =
        Pair(this.baseState, getOverlayId(this.overlayStack))

    override fun setStack(slot: Int, stack: ItemStack?) {
        val isOtherItem = slot >= OTHER_SLOTS_START

        if (isOtherItem) {
            OTHER_ITEM_DATA[this.getStack(slot).item]?.onRemove(this.world!!, this)
        }

        super.setStack(slot, stack)

        if (slot == BASE_SLOT) {
            this.baseState = (this.baseStack.item as? BlockItem)?.block?.defaultState
        } else if (isOtherItem) {
            OTHER_ITEM_DATA[stack?.item]?.onAdd(this.world!!, this)
        }
    }

    override fun removeStack(slot: Int): ItemStack {
        if (slot == BASE_SLOT) {
            this.baseState = null
        } else if (slot >= OTHER_SLOTS_START) {
            OTHER_ITEM_DATA[this.getStack(slot).item]?.onRemove(this.world!!, this)
        }

        return super.removeStack(slot)
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack {
        if (slot == BASE_SLOT) {
            this.baseState = null
        } else if (slot >= OTHER_SLOTS_START) {
            OTHER_ITEM_DATA[this.getStack(slot).item]?.onRemove(this.world!!, this)
        }

        return super.removeStack(slot, amount)
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
            this.world!!.updateNeighborsAlways(pos.offset(Direction.UP), this.ktBlock.value)
            val state = this.world!!.getBlockState(pos)
            this.world!!.updateListeners(pos, this.cachedState, state, 1)
        } else if (this.world?.isClient == true) {
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
            this.baseState = BlockState.CODEC.decode(Dynamic(NbtOps.INSTANCE, tag.get("state"))).result().get().first
        } else {
            this.baseState = null
        }
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        if (this.baseState == null) {
            tag?.remove("state")
        } else {
            tag?.put("state", BlockState.CODEC.encode(this.baseState, NbtOps.INSTANCE, CompoundTag()).get().left().get())
        }
        return super.toClientTag(tag)
    }

    override fun getMaxCountPerStack() =
        1

    override fun isValid(slot: Int, stack: ItemStack) =
        when (slot) {
            BASE_SLOT -> validForBase(stack, { s -> s.block.defaultState }, this.world!!, this.pos) != null
            OVERLAY_SLOT -> validForOverlay(stack)
            else -> validForOther(stack)
        }
}
