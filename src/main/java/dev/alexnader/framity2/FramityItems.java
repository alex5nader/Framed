package dev.alexnader.framity2;

import dev.alexnader.framity2.items.FramersHammer;
import net.minecraft.item.Item;
import net.minecraft.item.WallStandingBlockItem;
import net.minecraft.util.registry.Registry;

import static dev.alexnader.framity2.Framity2.BLOCKS;
import static dev.alexnader.framity2.Framity2.META;

@SuppressWarnings("unused")
public class FramityItems extends Registrar<Item> {
    public FramityItems() {
        super(Registry.ITEM);
    }

    public final FramersHammer FRAMERS_HAMMER = register(new FramersHammer(new Item.Settings().maxCount(1).group(META.MAIN_ITEM_GROUP)), META.id("framers_hammer"));

    public final WallStandingBlockItem TORCH_FRAME = register(
        new WallStandingBlockItem(BLOCKS.TORCH_FRAME, BLOCKS.WALL_TORCH_FRAME, new Item.Settings().group(META.MAIN_ITEM_GROUP)),
        META.id("torch_frame")
    );
}
