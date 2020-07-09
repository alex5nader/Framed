package dev.alexnader.framity.client.gui

import dev.alexnader.framity.SPECIAL_ITEMS
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
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.text.TranslatableText
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.function.BiFunction
import java.util.function.Predicate

class FrameGuiDescription(
    type: ScreenHandlerType<FrameGuiDescription>,
    syncId: Int,
    playerInventory: PlayerInventory?,
    context: ScreenHandlerContext,
    private val format: FrameDataFormat
) : SyncedGuiDescription(type, syncId, playerInventory, getBlockInventory(context, format.totalSize), getBlockPropertyDelegate(context)) {
    companion object {
        fun factory(format: FrameDataFormat, getType: () -> ScreenHandlerType<FrameGuiDescription>) =
            ScreenHandlerRegistry.SimpleClientHandlerFactory<FrameGuiDescription> { syncId, inventory ->
                FrameGuiDescription(getType(), syncId, inventory, ScreenHandlerContext.EMPTY, format)
            }
    }

    init {
        val root = WGridPanel(9)
        this.rootPanel = root

        val pos: BlockPos = context.run(BiFunction<World, BlockPos, BlockPos> { _, pos -> pos }).orNull() ?: BlockPos.ORIGIN

        root.add(
            WLabel(TranslatableText("gui.framity.frame.base_label")).apply {
                horizontalAlignment = HorizontalAlignment.CENTER
                verticalAlignment = VerticalAlignment.CENTER
            },
            1, 2, 8, 2
        )
        root.add(
            SingleItemSlot(blockInventory, format.base.start, format.base.size, 1).apply {
                filter = Predicate { stack -> validForBase(stack, { item -> item.block.defaultState }, world, pos) != null }
            },
            5 - format.base.size, 4
        )

        root.add(
            WLabel(TranslatableText("gui.framity.frame.overlay_label")).apply {
                horizontalAlignment = HorizontalAlignment.CENTER
                verticalAlignment = VerticalAlignment.CENTER
            },
            9, 2, 8, 2
        )
        root.add(
            SingleItemSlot(blockInventory, format.overlay.start, format.overlay.size, 1).apply {
                filter = Predicate{ stack -> validForOverlay(stack) }
            },
            13 - format.overlay.size, 4
        )

        root.add(
            WLabel(TranslatableText("gui.framity.frame.special_label")).apply {
                horizontalAlignment = HorizontalAlignment.CENTER
                verticalAlignment = VerticalAlignment.CENTER
            },
            0, 6, 18, 2
        )

        val specialSlotsX = 9 - format.special.size

        SPECIAL_ITEMS.forEach { (item, data) -> data.let { (offset, _) ->
            root.add(
                SingleItemSlot(blockInventory, format.special.applyOffset(offset)).apply {
                    filter = Predicate { stack -> stack.item == item }
                },
                specialSlotsX + offset * 2, 8
            )
        }}

        root.add(createPlayerInventoryPanel(), 0, 11)

        root.validate(this)
    }
}