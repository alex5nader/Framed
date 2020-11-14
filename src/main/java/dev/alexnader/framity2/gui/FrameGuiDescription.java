package dev.alexnader.framity2.gui;

import dev.alexnader.framity2.block.entity.FrameBlockEntity;
import io.github.cottonmc.cotton.gui.SyncedGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerContext;

import static dev.alexnader.framity2.Framity2.META;
import static dev.alexnader.framity2.Framity2.SPECIAL_ITEMS;
import static dev.alexnader.framity2.util.GuiUtil.*;

public class FrameGuiDescription extends SyncedGuiDescription {
    public FrameGuiDescription(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        this(
            syncId,
            playerInventory,
            context,
            context.run((world, pos) -> (FrameBlockEntity) world.getBlockEntity(pos))
                .orElseThrow(() -> new IllegalArgumentException("FrameGuiDescription can only be used with FrameBlockEntity."))
        );
    }

    private FrameGuiDescription(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, FrameBlockEntity frame) {
        super(META.FRAME_SCREEN_HANDLER_TYPE, syncId, playerInventory, SyncedGuiDescription.getBlockInventory(context, frame.size()), SyncedGuiDescription.getBlockPropertyDelegate(context));

        WGridPanel root = new WGridPanel(9);

        root.add(centered(label("gui.framity.base_label")), 1, 2, 8, 2);

        root.add(
            slotRow(SingleItemSlots::new, blockInventory, frame.sections().base()),
            5 - frame.sections().base().size(), 4
        );

        root.add(centered(label("gui.framity.overlay_label")), 9, 2, 8, 2);

        root.add(
            slotRow(SingleItemSlots::new, blockInventory, frame.sections().overlay()),
            13 - frame.sections().overlay().size(), 4
        );

        root.add(centered(label("gui.framity.frame.special_label")), 0, 6, 18, 2);

        SPECIAL_ITEMS.MAP.forEach((item, specialItem) -> root.add(
            new SingleItemSlots(blockInventory, frame.sections().special().makeAbsolute(specialItem.offset()), 1, 1, false),
            9 - frame.sections().special().size() + specialItem.offset() * 2, 8
        ));

        root.add(createPlayerInventoryPanel(), 0, 11);

        root.validate(this);
        rootPanel = root;
    }
}
