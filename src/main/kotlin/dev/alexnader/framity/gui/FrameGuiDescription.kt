package dev.alexnader.framity.gui

import dev.alexnader.framity.FRAME_SCREEN_HANDLER_TYPE
import dev.alexnader.framity.block_entities.FrameEntity
import dev.alexnader.framity.util.orNull
import dev.alexnader.framity.util.validForBase
import dev.alexnader.framity.util.validForOverlay
import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WLabel
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.TranslatableText
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.function.BiFunction
import java.util.function.Predicate

class FrameGuiDescription(
    syncId: Int,
    playerInventory: PlayerInventory?,
    context: ScreenHandlerContext
) : SyncedGuiDescription(FRAME_SCREEN_HANDLER_TYPE, syncId, playerInventory, getBlockInventory(context, FrameEntity.SLOT_COUNT), getBlockPropertyDelegate(context)) {
    companion object : ScreenHandlerRegistry.SimpleClientHandlerFactory<FrameGuiDescription> {
        override fun create(syncId: Int, inventory: PlayerInventory) =
            FrameGuiDescription(syncId, inventory, ScreenHandlerContext.EMPTY)
    }

    init {
        val root = WGridPanel(9)
        this.rootPanel = root

        val pos: BlockPos = context.run(BiFunction<World, BlockPos, BlockPos> { _, pos -> pos }).orNull() ?: BlockPos.ORIGIN

        val baseText = WLabel(TranslatableText("gui.framity.frame.base_label"))
        baseText.horizontalAlignment = HorizontalAlignment.CENTER
        baseText.verticalAlignment = VerticalAlignment.CENTER
        root.add(baseText, 1, 2, 8, 2)
        val baseSlot = SingleItemSlot(blockInventory, FrameEntity.BASE_SLOT)
        baseSlot.filter = Predicate { stack -> validForBase(stack, { item -> item.block.defaultState }, this.world, pos) != null }
        root.add(baseSlot, 4, 4)

        val overlayText = WLabel(TranslatableText("gui.framity.frame.overlay_label"))
        overlayText.horizontalAlignment = HorizontalAlignment.CENTER
        overlayText.verticalAlignment = VerticalAlignment.CENTER
        root.add(overlayText, 9, 2, 8, 2)
        val overlaySlot = SingleItemSlot(blockInventory, FrameEntity.OVERLAY_SLOT)
        overlaySlot.filter = Predicate{ stack -> validForOverlay(stack) }
        root.add(overlaySlot, 12, 4)

        val otherText = WLabel(TranslatableText("gui.framity.frame.other_label"))
        otherText.horizontalAlignment = HorizontalAlignment.CENTER
        otherText.verticalAlignment = VerticalAlignment.CENTER
        root.add(otherText, 0, 6, 18, 2)

        val otherSlotsCount = FrameEntity.OTHER_ITEM_DATA.size
        val otherSlotsStart = 9 - otherSlotsCount

        FrameEntity.OTHER_ITEM_DATA.forEach { (item, data) ->
            val otherSlot = SingleItemSlot(blockInventory, FrameEntity.OTHER_SLOTS_START + data.offset)
            otherSlot.filter = Predicate { stack -> stack.item == item }
            root.add(otherSlot, otherSlotsStart + data.offset * 2, 8)
        }

        root.add(createPlayerInventoryPanel(), 0, 11)

        root.validate(this)
    }
}