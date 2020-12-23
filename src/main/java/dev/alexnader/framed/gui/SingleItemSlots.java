package dev.alexnader.framed.gui;

import io.github.cottonmc.cotton.gui.ValidatedSlot;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import net.minecraft.inventory.Inventory;

public class SingleItemSlots extends WItemSlot {
    public SingleItemSlots(final Inventory inventory, final int startIndex, final int slotsWide, final int slotsHigh, final boolean big) {
        super(inventory, startIndex, slotsWide, slotsHigh, big);
    }

    @Override
    protected ValidatedSlot createSlotPeer(final Inventory inventory, final int index, final int x, final int y) {
        return new ValidatedSingleItemSlot(inventory, index, x, y);
    }
}
