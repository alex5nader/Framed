package dev.alexnader.framity

import dev.alexnader.framity.data.overlay
import dev.alexnader.framity.gui.FrameGuiDescription
import dev.alexnader.framity.item.{FramersHammer, registerItems}
import dev.alexnader.framity.block.registerBlocks
import dev.alexnader.framity.block_entity.registerBlockEntityTypes
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry
import net.minecraft.item.ItemStack
import net.minecraft.resource.ResourceType
import net.minecraft.screen.{ScreenHandlerContext, ScreenHandlerType}
import net.minecraft.util.Identifier
import org.apache.logging.log4j.{LogManager, Logger}

import scala.jdk.CollectionConverters._

object Framity extends ModInitializer {
  val namespace: String = "framity"

  def id(path: String) = new Identifier(namespace, path)

  val Logger: Logger = LogManager.getLogger("Framity")

  var FrameScreenHandlerType: ScreenHandlerType[FrameGuiDescription] = _

  override def onInitialize(): Unit = {
    registerBlockEntityTypes()
    val items = registerBlocks() ++ registerItems()

    FabricItemGroupBuilder.create(id("framity"))
      .appendItems { list => list.addAll(items.map { new ItemStack(_) }.asJava) }
      .icon(() => new ItemStack(FramersHammer))
      .build()

    FrameScreenHandlerType = ScreenHandlerRegistry.registerExtended(id("frame_screen_handler"), (syncId, inventory, buf) =>
      new FrameGuiDescription(syncId, inventory, ScreenHandlerContext.create(inventory.player.world, buf.readBlockPos))
    )

    ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(overlay.Listener)
  }
}
