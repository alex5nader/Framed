package dev.alexnader.framity2.gui;

import io.github.cottonmc.cotton.gui.ValidatedSlot;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class ValidatedSingleItemSlot extends ValidatedSlot {
    public ValidatedSingleItemSlot(final Inventory inventory, final int index, final int x, final int y) {
        super(inventory, index, x, y);
    }

    @Override
    public int getMaxItemCount() {
        return 1;
    }

    @Override
    public int getMaxItemCount(final ItemStack stack) {
        return 1;
    }
}
