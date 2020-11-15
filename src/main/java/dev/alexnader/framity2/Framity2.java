package dev.alexnader.framity2;

import com.mojang.serialization.Codec;
import dev.alexnader.framity2.data.OverlayDataListener;
import dev.alexnader.framity2.items.SpecialItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class Framity2 implements ModInitializer {
    public static FramityProperties PROPERTIES;
    public static SpecialItems SPECIAL_ITEMS;

    public static FramityMeta META;
    public static FramityBlocks BLOCKS;
    public static FramityItems ITEMS;
    public static FramityBlockEntityTypes BLOCK_ENTITY_TYPES;

    public static OverlayDataListener OVERLAYS;

    @Override
    public void onInitialize() {
        PROPERTIES = new FramityProperties();
        SPECIAL_ITEMS = new SpecialItems();

        META = new FramityMeta();
        BLOCKS = new FramityBlocks();
        ITEMS = new FramityItems();
        BLOCK_ENTITY_TYPES = new FramityBlockEntityTypes();

        OVERLAYS = new OverlayDataListener();
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(OVERLAYS);
    }
}
