package dev.alexnader.framity.client.gui

import dev.alexnader.framity.FRAME_SCREEN_HANDLER_TYPE
import dev.alexnader.framity.SPECIAL_ITEMS
import dev.alexnader.framity.block_entities.FrameEntity
import dev.alexnader.framity.blocks.validForBase
import dev.alexnader.framity.blocks.validForOverlay
import dev.alexnader.framity.util.orNull
import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WLabel
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.TranslatableText
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.function.BiFunction
import java.util.function.Predicate

class FrameGuiDescription private constructor(
    syncId: Int,
    playerInventory: PlayerInventory?,
    context: ScreenHandlerContext,
    private val frameEntity: FrameEntity
) : SyncedGuiDescription(FRAME_SCREEN_HANDLER_TYPE, syncId, playerInventory, getBlockInventory(context, frameEntity.format.totalSize), getBlockPropertyDelegate(context)) {
    constructor(syncId: Int, playerInventory: PlayerInventory?, context: ScreenHandlerContext) : this(
        syncId,
        playerInventory,
        context,
        context.run(BiFunction<World, BlockPos, FrameEntity?> { world, pos ->
            world.getBlockEntity(pos) as? FrameEntity?
        }).orNull()
            ?: error("Frame GUI can only be used for frame entities.")
    )

    init {
        val root = WGridPanel(9)
        this.rootPanel = root

        root.add(
            WLabel(TranslatableText("gui.framity.frame.base_label")).apply {
                horizontalAlignment = HorizontalAlignment.CENTER
                verticalAlignment = VerticalAlignment.CENTER
            },
            1, 2, 8, 2
        )
        root.add(
            SingleItemSlot(blockInventory, frameEntity.format.base.start, frameEntity.format.base.size, 1).apply {
                filter = Predicate { stack -> validForBase(stack, { item -> item.block.defaultState }, world, frameEntity.pos) != null }
            },
            5 - frameEntity.format.base.size, 4
        )

        root.add(
            WLabel(TranslatableText("gui.framity.frame.overlay_label")).apply {
                horizontalAlignment = HorizontalAlignment.CENTER
                verticalAlignment = VerticalAlignment.CENTER
            },
            9, 2, 8, 2
        )
        root.add(
            SingleItemSlot(blockInventory, frameEntity.format.overlay.start, frameEntity.format.overlay.size, 1).apply {
                filter = Predicate{ stack -> validForOverlay(stack) }
            },
            13 - frameEntity.format.overlay.size, 4
        )

        root.add(
            WLabel(TranslatableText("gui.framity.frame.special_label")).apply {
                horizontalAlignment = HorizontalAlignment.CENTER
                verticalAlignment = VerticalAlignment.CENTER
            },
            0, 6, 18, 2
        )

        val specialSlotsX = 9 - frameEntity.format.special.size

        SPECIAL_ITEMS.forEach { (item, data) -> data.let { (offset, _) ->
            root.add(
                SingleItemSlot(blockInventory, frameEntity.format.special.applyOffset(offset)).apply {
                    filter = Predicate { stack -> stack.item == item }
                },
                specialSlotsX + offset * 2, 8
            )
        }}

        root.add(createPlayerInventoryPanel(), 0, 11)

        root.validate(this)
    }
}