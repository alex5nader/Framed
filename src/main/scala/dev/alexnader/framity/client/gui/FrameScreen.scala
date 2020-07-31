package dev.alexnader.framity.client.gui

import dev.alexnader.framity.gui.FrameGuiDescription
import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry
import net.minecraft.entity.player.{PlayerEntity, PlayerInventory}
import net.minecraft.text.Text

object FrameScreen extends ScreenRegistry.Factory[FrameGuiDescription, FrameScreen] {
  override def create(desc: FrameGuiDescription, playerInventory: PlayerInventory, title: Text): FrameScreen = new FrameScreen(desc, playerInventory.player, title)
}

class FrameScreen(desc: FrameGuiDescription, player: PlayerEntity, title: Text) extends CottonInventoryScreen(desc, player, title)
