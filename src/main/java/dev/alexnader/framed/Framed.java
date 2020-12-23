package dev.alexnader.framed;

import dev.alexnader.framed.data.OverlayDataListener;
import dev.alexnader.framed.items.SpecialItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

public class Framed implements ModInitializer {
    public static FramedProperties PROPERTIES;
    public static SpecialItems SPECIAL_ITEMS;

    public static FramedMeta META;
    public static FramedBlocks BLOCKS;
    public static FramedItems ITEMS;
    public static FramedBlockEntityTypes BLOCK_ENTITY_TYPES;

    public static OverlayDataListener OVERLAYS;

    @Override
    public void onInitialize() {
        PROPERTIES = new FramedProperties();
        SPECIAL_ITEMS = new SpecialItems();

        META = new FramedMeta();
        BLOCKS = new FramedBlocks();
        ITEMS = new FramedItems();
        BLOCK_ENTITY_TYPES = new FramedBlockEntityTypes();

        OVERLAYS = new OverlayDataListener();
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(OVERLAYS);
    }
}
