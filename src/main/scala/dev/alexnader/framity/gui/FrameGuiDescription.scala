package dev.alexnader.framity.gui

import dev.alexnader.framity.Framity
import dev.alexnader.framity.block.frame.FrameData.Sections.SlotSectionConversions
import dev.alexnader.framity.block.frame.SpecialItems
import dev.alexnader.framity.block.frame.Valid.ItemStackValidQuery
import dev.alexnader.framity.block_entity.FrameEntity
import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandlerContext

import scala.jdk.OptionConverters._

class FrameGuiDescription private(syncId: Int, playerInventory: PlayerInventory, context: ScreenHandlerContext, frameEntity: FrameEntity)
  extends SyncedGuiDescription(Framity.FrameScreenHandlerType, syncId, playerInventory, SyncedGuiDescription.getBlockInventory(context, frameEntity.sections.itemCount), SyncedGuiDescription.getBlockPropertyDelegate(context)) {
  def this(syncId: Int, playerInventory: PlayerInventory, context: ScreenHandlerContext) =
    this(syncId, playerInventory, context, context.run[Option[FrameEntity]]((world, pos) => world.getBlockEntity(pos) match {
      case e: FrameEntity => Some(e)
      case _ => None
    }).toScala.flatten.getOrElse(throw new IllegalStateException("Frame GUI can only be used for frame entities.")))

  rootPanel = {
    def baseFilter(stack: ItemStack): Boolean = stack.isValidForBase(bi => Some(bi.getBlock.getDefaultState), world, frameEntity.getPos).isDefined

    import dsl.Add._
    import dsl.GridPanel._
    import dsl.Label._
    import dsl.Slot._
    import dsl.Text._
    import dsl.Validate._

    val root = gridPanel ofSize 9

    add (centered (label containing (translatable text "gui.framity.frame.base_label"))) to root at (1, 2, 8, 2)
    add (slotRow using SingleItemSlot withFilter baseFilter linkedTo blockInventory overRange frameEntity.sections.base) to root at (5 - frameEntity.sections.base.size, 4)

    add (centered (label containing (translatable text "gui.framity.frame.overlay_label"))) to root at (9, 2, 8, 2)
    add (slotRow using SingleItemSlot withFilter (_.isValidForOverlay) linkedTo blockInventory overRange frameEntity.sections.overlay) to root at (13 - frameEntity.sections.overlay.size, 4)

    add (centered (label containing (translatable text "gui.framity.frame.special_label"))) to root at (0, 6, 18, 2)
    SpecialItems.map foreach { case (item, data) =>
      add (slot using SingleItemSlot withFilter (_.getItem eq item) linkedTo blockInventory at data.offset.toAbsolute(frameEntity.sections.special)) to root at(9 - frameEntity.sections.special.size + data.offset * 2, 8)
    }

    add (createPlayerInventoryPanel()) to root at (0, 11)

    validate (root) using this

    root
  }
}
