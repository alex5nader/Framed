package dev.alexnader.framity.mixin;

import dev.alexnader.framity.block_entities.FrameEntity;
import dev.alexnader.framity.blocks.BlockFrame;
import dev.alexnader.framity.blocks.SlabFrame;
import dev.alexnader.framity.blocks.StairsFrame;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

import static dev.alexnader.framity.FramityKt.FRAMERS_HAMMER;
import static dev.alexnader.framity.util.FramifyHelperKt.*;
import static dev.alexnader.framity.util.FrameContentsKt.*;

@SuppressWarnings("unused")
@Mixin({BlockFrame.class, SlabFrame.class, StairsFrame.class})
public class Framify extends Block {
    private Framify() {
        super(FabricBlockSettings.of(Material.AIR)); // dummy, never used
    }

    @SuppressWarnings("unused")
    @Inject(method = "<init>*", at = @At("RETURN"))
    private void constructorProxy(CallbackInfo ci) {
        this.setDefaultState(this.getDefaultState().with(HasGlowstone, false));
    }

    @SuppressWarnings("unused")
    @Inject(method = "appendProperties", at = @At("HEAD"))
    private void appendPropertiesProxy(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(HasGlowstone);
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView view, BlockPos pos) {
        return true;
    }

    @Override
    public boolean hasDynamicBounds() {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    @Environment(EnvType.CLIENT)
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return 1.0F;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        super.onSyncedBlockEvent(state, world, pos, type, data);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity != null && blockEntity.onSyncedBlockEvent(type, data);
    }

    @SuppressWarnings("deprecation")
    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity instanceof NamedScreenHandlerFactory ? (NamedScreenHandlerFactory)blockEntity : null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (world == null || world.isClient() || pos == null || state == null || state.getBlock() == newState.getBlock()) {
            return;
        }

        if (posToPlayer.containsKey(pos)) {
            PlayerEntity player = posToPlayer.remove(pos);

            if (player.isSneaking() && player.getStackInHand(player.getActiveHand()).getItem() == FRAMERS_HAMMER.getValue()) {
                onHammerRemove(world, (FrameEntity<?>) world.getBlockEntity(pos), state, player, false);
            }
        } else {
            BlockEntity blockEntity = world.getBlockEntity(pos);

            if (blockEntity instanceof Inventory) {
                ItemScatterer.spawn(world, pos, (Inventory) blockEntity);
                world.updateComparators(pos, this);
            }

            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        super.onBlockBreakStart(state, world, pos, player);

        if (world == null || world.isClient() || pos == null || player == null || state == null) {
            return;
        }

        if (player.isSneaking() && player.getStackInHand(player.getActiveHand()).getItem() == FRAMERS_HAMMER.getValue()) {
            onHammerRemove(world, (FrameEntity<?>) world.getBlockEntity(pos), state, player, true);
        }
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);

        if (world.isClient() || pos == null || player == null || !player.isCreative()) {
            return;
        }

        posToPlayer.put(pos, player);
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.CONSUME;
        }

        FrameEntity<?> frameEntity = (FrameEntity<?>) world.getBlockEntity(pos);

        if (frameEntity == null) {
            return ActionResult.CONSUME;
        }

        ItemStack playerStack = player.getStackInHand(player.getActiveHand());

        if (frameEntity.getOverlayStack().isEmpty() && validForOverlay(playerStack)) {
            frameEntity.copyFrom(FrameEntity.OVERLAY_SLOT, playerStack, 1, !player.isCreative());
            frameEntity.markDirty();

            return ActionResult.SUCCESS;
        }

        BlockState maybeBaseState = validForBase(
                playerStack,
                (bi) -> bi.getBlock().getPlacementState(new ItemPlacementContext(new ItemUsageContext(player, hand, hit))),
                world,
                pos
        );

        if (playerStack.getItem() != frameEntity.getBaseStack().getItem() && maybeBaseState != null) {
            if (!frameEntity.getBaseStack().isEmpty() && !player.isCreative()) {
                player.inventory.offerOrDrop(world, frameEntity.getBaseStack());
            }

            frameEntity.copyFrom(FrameEntity.BASE_SLOT, playerStack, 1, !player.isCreative());
            frameEntity.setBaseState(maybeBaseState);

            return ActionResult.SUCCESS;
        }

        if (validForOther(playerStack)) {
            int slot = FrameEntity.getSlotForOtherItem(playerStack.getItem());

            if (frameEntity.getStack(slot).isEmpty()) {
                frameEntity.copyFrom(slot, playerStack, 1, !player.isCreative());

                return ActionResult.SUCCESS;
            } else {
                return ActionResult.CONSUME;
            }
        }

        if (playerStack.isEmpty()) {
            player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
            return ActionResult.SUCCESS;
        }

        return ActionResult.CONSUME;
    }
}
