package dev.alexnader.framity2.gui;

import io.github.cottonmc.cotton.gui.ValidatedSlot;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import net.minecraft.inventory.Inventory;

public class SingleItemSlots extends WItemSlot {
    public SingleItemSlots(Inventory inventory, int startIndex, int slotsWide, int slotsHigh, boolean big) {
        super(inventory, startIndex, slotsWide, slotsHigh, big);
    }

    @Override
    protected ValidatedSlot createSlotPeer(Inventory inventory, int index, int x, int y) {
        return new ValidatedSingleItemSlot(inventory, index, x, y);
    }
}
