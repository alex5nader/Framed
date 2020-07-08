package dev.alexnader.framity.client.gui

import dev.alexnader.framity.FRAME_SCREEN_HANDLER_TYPE
import dev.alexnader.framity.SPECIAL_ITEM_DATA
import dev.alexnader.framity.block_entities.FrameEntity
import dev.alexnader.framity.blocks.validForBase
import dev.alexnader.framity.blocks.validForOverlay
import dev.alexnader.framity.util.FrameDataFormat
import dev.alexnader.framity.util.orNull
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
    context: ScreenHandlerContext,
    private val format: FrameDataFormat
) : SyncedGuiDescription(FRAME_SCREEN_HANDLER_TYPE, syncId, playerInventory, getBlockInventory(context, format.totalSize), getBlockPropertyDelegate(context)) {
    companion object {
        fun factory(format: FrameDataFormat) =
            ScreenHandlerRegistry.SimpleClientHandlerFactory<FrameGuiDescription> { syncId, inventory -> FrameGuiDescription(syncId, inventory, ScreenHandlerContext.EMPTY, format) }
    }

    init {
        val root = WGridPanel(9)
        this.rootPanel = root

        val pos: BlockPos = context.run(BiFunction<World, BlockPos, BlockPos> { _, pos -> pos }).orNull() ?: BlockPos.ORIGIN

        val baseText = WLabel(TranslatableText("gui.framity.frame.base_label"))
        baseText.horizontalAlignment = HorizontalAlignment.CENTER
        baseText.verticalAlignment = VerticalAlignment.CENTER
        root.add(baseText, 1, 2, 8, 2)
        val baseSlot = SingleItemSlot(blockInventory, format.base.start)
        baseSlot.filter = Predicate { stack -> validForBase(stack, { item -> item.block.defaultState }, this.world, pos) != null }
        root.add(baseSlot, 4, 4)

        val overlayText = WLabel(TranslatableText("gui.framity.frame.overlay_label"))
        overlayText.horizontalAlignment = HorizontalAlignment.CENTER
        overlayText.verticalAlignment = VerticalAlignment.CENTER
        root.add(overlayText, 9, 2, 8, 2)
        val overlaySlot = SingleItemSlot(blockInventory, format.overlay.start)
        overlaySlot.filter = Predicate{ stack -> validForOverlay(stack) }
        root.add(overlaySlot, 12, 4)

        val specialText = WLabel(TranslatableText("gui.framity.frame.special_label"))
        specialText.horizontalAlignment = HorizontalAlignment.CENTER
        specialText.verticalAlignment = VerticalAlignment.CENTER
        root.add(specialText, 0, 6, 18, 2)

        val specialSlotsX = 9 - format.special.size

        SPECIAL_ITEM_DATA.forEach { (item, data) -> data.let { (offset, _) ->
            val specialSlot = SingleItemSlot(blockInventory, format.special.applyOffset(offset))
            specialSlot.filter = Predicate { stack -> stack.item == item }
            root.add(specialSlot, specialSlotsX + offset * 2, 8)
        }}

        root.add(createPlayerInventoryPanel(), 0, 11)

        root.validate(this)
    }
}