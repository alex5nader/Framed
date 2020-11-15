package dev.alexnader.framity2.client.gui;

import dev.alexnader.framity2.gui.FrameGuiDescription;
import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class FrameScreen extends CottonInventoryScreen<FrameGuiDescription> {
    public static final ScreenRegistry.Factory<FrameGuiDescription, FrameScreen> FACTORY =
        (desc, playerInventory, title) -> new FrameScreen(desc, playerInventory.player, title);

    public FrameScreen(final FrameGuiDescription description, final PlayerEntity player, final Text title) {
        super(description, player, title);
    }
}
