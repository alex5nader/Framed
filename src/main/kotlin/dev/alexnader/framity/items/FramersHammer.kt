package dev.alexnader.framity.items

import dev.alexnader.framity.block_entities.FrameEntity
import dev.alexnader.framity.blocks.Frame
import dev.alexnader.framity.util.FrameData
import net.minecraft.block.BlockState
import net.minecraft.client.item.ModelPredicateProvider
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

enum class HammerMode {
    NONE {
        override val next get() = PARTIAL_COPY
        override val translationKey get() = "no_copy_mode"
    },
    PARTIAL_COPY {
        override val next get() = WHOLE_COPY
        override val translationKey get() = "partial_copy_mode"
    },
    WHOLE_COPY {
        override val next get() = NONE
        override val translationKey get() = "whole_copy_mode"
    };

    companion object {
        fun fromString(mode: String) =
            when (mode) {
                "NONE" -> NONE
                "PARTIAL_COPY" -> PARTIAL_COPY
                "WHOLE_COPY" -> WHOLE_COPY
                else -> null
            }

        fun default() = NONE
    }

    abstract val next: HammerMode
    abstract val translationKey: String
}

data class HammerData(val storedData: FrameData?, val mode: HammerMode) {
    companion object {
        fun fromTag(tag: CompoundTag) =
            HammerData(
                if ("storedData" in tag) {
                    FrameData.fromTag(tag.getCompound("storedData"))
                } else {
                    null
                },
                HammerMode.fromString(tag.getString("mode")) ?: HammerMode.default()
            )
    }

    /**
     * Returns null on success, onFail() on fail
     */
    fun <T> applySettings(frame: Frame, state: BlockState, frameEntity: FrameEntity, player: PlayerEntity, world: World, onFail: () -> T): T? {
        val storedData = this.storedData ?: return onFail()
        val playerInventory = player.inventory ?: return onFail()

        if (storedData.format != frameEntity.format) {
            player.sendMessage(TranslatableText("gui.framity.framers_hammer.different_format"), true)
            return onFail()
        }

        if (!player.isCreative) {
            val requireAllItems = when (mode) {
                HammerMode.NONE -> return onFail()
                HammerMode.WHOLE_COPY -> true
                HammerMode.PARTIAL_COPY -> false
            }

            val itemToFrameSlot = storedData.items
                .mapIndexed { slot, storedStack -> Pair(storedStack.item, slot) }
                .toMap()

            @Suppress("MapGetWithNotNullAssertionOperator")
            val playerSlotToFrameSlot = (0 until playerInventory.size())
                .asSequence()
                .map { slot -> Pair(slot, playerInventory.getStack(slot)) }
                .filter { (_, playerStack) ->
                    !playerStack.isEmpty && playerStack.item in itemToFrameSlot
                }
                .map { (slot, playerStack) -> Pair(slot, itemToFrameSlot[playerStack.item]!!) }
                .toMap()

            if (requireAllItems && playerSlotToFrameSlot.size != storedData.items.count { !it.isEmpty }) {
                return onFail()
            }

            if (!world.isClient) {
                playerSlotToFrameSlot.forEach { (playerSlot, frameSlot) ->
                    if (frameEntity.getStack(frameSlot).item != playerInventory.getStack(playerSlot).item
                        && frame.slotIsValid(state, frameSlot)) {
                        if (!frameEntity.getStack(frameSlot).isEmpty) {
                            player.inventory.offerOrDrop(world, frameEntity.removeStack(frameSlot))
                        }
                        frameEntity.setStack(frameSlot, playerInventory.removeStack(playerSlot, 1))
                    }
                }
            }
        } else {
            if (mode == HammerMode.NONE) {
                return onFail()
            }

            if (!world.isClient) {
                storedData.items.forEachIndexed { slot, storedStack ->
                    frameEntity.setStack(slot, storedStack.copy())
                }
            }
        }

        player.sendMessage(TranslatableText("gui.framity.framers_hammer.apply_settings"), true)

        return null
    }
}

/**
 * [Item] subclass for the framer's hammer.
 */
class FramersHammer : Item(Settings().maxCount(1)) {
    companion object {
        val DEFAULT_TAG = CompoundTag().apply {
            this.putString("mode", HammerMode.default().toString())
        }

        object ModelPredicate : ModelPredicateProvider {
            override fun call(stack: ItemStack, world: ClientWorld?, entity: LivingEntity?) =
                stack.tag?.let { tag ->
                    when (HammerMode.fromString(tag.getString("mode"))) {
                        HammerMode.NONE -> 0f
                        HammerMode.PARTIAL_COPY -> 1f
                        HammerMode.WHOLE_COPY -> 2f
                        null -> 0f
                    }
                } ?: 0f
        }
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val stack = context.stack ?: return super.useOnBlock(context)
        if (stack.tag == null) {
            stack.tag = DEFAULT_TAG
        }
        val tag = stack.tag ?: error("Tag is null")
        val hammerData = HammerData.fromTag(tag)
        val player = context.player ?: return super.useOnBlock(context)
        val world = context.world ?: return super.useOnBlock(context)
        val pos = context.blockPos ?: return super.useOnBlock(context)
        val state = world.getBlockState(pos)
        val frame = state.block as? Frame ?: return super.useOnBlock(context)
        val frameEntity = world.getBlockEntity(pos) as? FrameEntity ?: return super.useOnBlock(context)

        return if (player.isSneaking) {
            player.sendMessage(TranslatableText("gui.framity.framers_hammer.copy_settings"), true)
            tag.put("storedData", frameEntity.data.toTag())

            ActionResult.SUCCESS
        } else {
            hammerData.applySettings(frame, state, frameEntity, player, world) { super.useOnBlock(context) }
                ?: ActionResult.SUCCESS
        }
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        if (!user.isSneaking) {
            return super.use(world, user, hand)
        }

        val stack = user.getStackInHand(hand) ?: return super.use(world, user, hand)
        val tag = stack.tag ?: DEFAULT_TAG
        val mode = HammerMode.fromString(tag.getString("mode")) ?: HammerMode.default()

        val newMode = mode.next
        tag.putString("mode", newMode.toString())
        user.sendMessage(TranslatableText("gui.framity.framers_hammer.${newMode.translationKey}"), true)

        return TypedActionResult.success(stack)
    }
}
