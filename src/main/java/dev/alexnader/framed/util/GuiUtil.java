package dev.alexnader.framed.util;

import com.mojang.datafixers.util.Function5;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment;
import net.minecraft.inventory.Inventory;
import net.minecraft.text.TranslatableText;

public enum GuiUtil {
    ;

    public static WLabel label(final String translationKey) {
        return new WLabel(new TranslatableText(translationKey));
    }

    public static <L extends WLabel> L centered(final L label) {
        label.setHorizontalAlignment(HorizontalAlignment.CENTER);
        label.setVerticalAlignment(VerticalAlignment.CENTER);
        return label;
    }

    public static <S extends WItemSlot> S slotRow(
        final Function5<Inventory, Integer, Integer, Integer, Boolean, S> constructor,
        final Inventory inventory,
        final Section section
    ) {
        return constructor.apply(inventory, section.start(), section.size(), 1, false);
    }
}
