package dev.alexnader.framity.client.gui

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text

class FrameScreen(description: FrameGuiDescription, player: PlayerEntity, title: Text) :
    CottonInventoryScreen<FrameGuiDescription>(description, player, title) {
    companion object : ScreenRegistry.Factory<FrameGuiDescription, FrameScreen> {
        override fun create(handler: FrameGuiDescription, inventory: PlayerInventory, title: Text) =
            FrameScreen(handler, inventory.player, title)
    }
}