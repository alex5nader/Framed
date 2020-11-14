package dev.alexnader.framity2;

import dev.alexnader.framity2.client.assets.OverlayAssetListener;
import dev.alexnader.framity2.data.OverlayDataListener;
import dev.alexnader.framity2.items.SpecialItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

public class Framity2 implements ModInitializer {
    public static FramityMeta META;
    public static FramityProperties PROPERTIES;
    public static FramityItems ITEMS;
    public static FramityBlocks BLOCKS;
    public static FramityBlockEntityTypes BLOCK_ENTITY_TYPES;

    public static FramityCodecs CODECS;

    public static OverlayDataListener OVERLAYS;

    public static SpecialItems SPECIAL_ITEMS;

    @Override
    public void onInitialize() {
        META = new FramityMeta();
        PROPERTIES = new FramityProperties();
        ITEMS = new FramityItems();
        BLOCKS = new FramityBlocks();
        BLOCK_ENTITY_TYPES = new FramityBlockEntityTypes();

        CODECS = new FramityCodecs();

        OVERLAYS = new OverlayDataListener();
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(OVERLAYS);

        SPECIAL_ITEMS = new SpecialItems();
    }
}
