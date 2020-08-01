package dev.alexnader.framity

import dev.alexnader.framity.data.overlay
import dev.alexnader.framity.gui.FrameGuiDescription
import dev.alexnader.framity.item.{FramersHammer, addItems}
import dev.alexnader.framity.block.addBlocks
import dev.alexnader.framity.block_entity.addBlockEntityTypes
import dev.alexnader.framity.mod.{ItemGroupAdder, Registerer}
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry
import net.minecraft.item.ItemStack
import net.minecraft.resource.ResourceType
import net.minecraft.screen.{ScreenHandlerContext, ScreenHandlerType}
import org.apache.logging.log4j.{LogManager, Logger}

object Framity extends ModInitializer {
  implicit val Mod: Registerer = Registerer `for` "framity"

  val Logger: Logger = LogManager.getLogger("Framity")

  val ItemGroup: ItemGroupAdder = new ItemGroupAdder("framity", () => new ItemStack(FramersHammer)) addTo Mod

  lazy val FrameScreenHandlerType: ScreenHandlerType[FrameGuiDescription] =
    ScreenHandlerRegistry.registerExtended(Mod.id("frame_screen_handler"), (syncId, inventory, buf) =>
      new FrameGuiDescription(syncId, inventory, ScreenHandlerContext.create(inventory.player.world, buf.readBlockPos))
    )

  override def onInitialize(): Unit = {
    addBlocks
    addBlockEntityTypes
    addItems

    Mod.register()

    FrameScreenHandlerType

    ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(overlay.Listener)
  }
}
