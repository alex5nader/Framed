package dev.alexnader.framed;

import dev.alexnader.framed.block.frame.*;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.function.Function;

import static dev.alexnader.framed.Framed.META;

public class FramedBlocks extends Registrar<Block> {
    public final BlockFrame BLOCK_FRAME;
    public final SlabFrame SLAB_FRAME;
    public final StairsFrame STAIRS_FRAME;
    public final FenceFrame FENCE_FRAME;
    public final FenceGateFrame FENCE_GATE_FRAME;
    public final TrapdoorFrame TRAPDOOR_FRAME;
    public final DoorFrame DOOR_FRAME;
    public final PathFrame PATH_FRAME;
    public final TorchFrame TORCH_FRAME;
    public final WallTorchFrame WALL_TORCH_FRAME;
    public final PressurePlateFrame PRESSURE_PLATE_FRAME;
    public final WallFrame WALL_FRAME;
    public final LayerFrame LAYER_FRAME;
    public final CarpetFrame CARPET_FRAME;

    private <A extends Block> A registerWithItem(final A block, final Identifier id, final Item.Settings settings) {
        Registry.register(Registry.ITEM, id, new BlockItem(block, settings));
        return register(block, id);
    }

    public FramedBlocks() {
        super(Registry.BLOCK);

        final Function<Material, AbstractBlock.Settings> makeSettings = material ->
            FabricBlockSettings
                .of(material)
                .hardness(0.33f)
                .sounds(BlockSoundGroup.WOOD)
                .nonOpaque()
                .solidBlock((a, b, c) -> false)
                .luminance(state -> Boolean.TRUE.equals(state.get(Properties.LIT)) ? 15 : 0);

        final AbstractBlock.Settings frameSettings = makeSettings.apply(Material.WOOD);
        final AbstractBlock.Settings torchFrameSettings = makeSettings.apply(Material.SUPPORTED)
            .noCollision()
            .breakInstantly()
            .luminance(state -> Boolean.TRUE.equals(state.get(Properties.LIT)) ? 15 : 14);

        final Item.Settings itemSettings = new Item.Settings()
            .group(META.MAIN_ITEM_GROUP);

        BLOCK_FRAME = registerWithItem(new BlockFrame(frameSettings), META.id("block_frame"), itemSettings);
        SLAB_FRAME = registerWithItem(new SlabFrame(frameSettings), META.id("slab_frame"), itemSettings);
        STAIRS_FRAME = registerWithItem(new StairsFrame(BLOCK_FRAME.getDefaultState(), frameSettings), META.id("stairs_frame"), itemSettings);
        FENCE_FRAME = registerWithItem(new FenceFrame(frameSettings), META.id("fence_frame"), itemSettings);
        FENCE_GATE_FRAME = registerWithItem(new FenceGateFrame(frameSettings), META.id("fence_gate_frame"), itemSettings);
        TRAPDOOR_FRAME = registerWithItem(new TrapdoorFrame(frameSettings.allowsSpawning((a, b, c, d) -> false)), META.id("trapdoor_frame"), itemSettings);
        DOOR_FRAME = registerWithItem(new DoorFrame(frameSettings), META.id("door_frame"), itemSettings);
        PATH_FRAME = registerWithItem(new PathFrame(frameSettings), META.id("path_frame"), itemSettings);
        PRESSURE_PLATE_FRAME = registerWithItem(new PressurePlateFrame(frameSettings), META.id("pressure_plate_frame"), itemSettings);
        WALL_FRAME = registerWithItem(new WallFrame(frameSettings), META.id("wall_frame"), itemSettings);
        LAYER_FRAME = registerWithItem(new LayerFrame(frameSettings), META.id("layer_frame"), itemSettings);
        CARPET_FRAME = registerWithItem(new CarpetFrame(frameSettings), META.id("carpet_frame"), itemSettings);

        TORCH_FRAME = register(new TorchFrame(torchFrameSettings), META.id("torch_frame"));
        WALL_TORCH_FRAME = register(new WallTorchFrame(torchFrameSettings), META.id("wall_torch_frame"));
    }
}
