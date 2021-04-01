package dev.alexnader.framed.mixin.local;

import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Unit;
import dev.alexnader.framed.block.FrameSlotInfo;
import dev.alexnader.framed.block.entity.FrameBlockEntity;
import dev.alexnader.framed.block.frame.*;
import dev.alexnader.framed.items.FramersHammer;
import dev.alexnader.framed.items.SpecialItems;
import dev.alexnader.framed.util.ValidQuery;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

import static dev.alexnader.framed.Framed.*;
import static dev.alexnader.framed.util.ValidQuery.checkIf;

@Mixin({
    BlockFrame.class,
    SlabFrame.class,
    StairsFrame.class,
    FenceFrame.class,
    FenceGateFrame.class,
    TrapdoorFrame.class,
    DoorFrame.class,
    PathFrame.class,
    TorchFrame.class,
    WallTorchFrame.class,
    PressurePlateFrame.class,
    WallFrame.class,
    LayerFrame.class,
    CarpetFrame.class,
    PaneFrame.class
})
public abstract class FrameBehaviour extends Block implements BlockEntityProvider, Frame, FrameSlotInfo {
    @SuppressWarnings("unused") // required by mixin
    private FrameBehaviour(final Settings settings) {
        super(settings);
        throw new IllegalStateException("Mixin constructor should never run.");
    }

    @Inject(method = "<init>*", at = @At("TAIL"))
    void setFramePropertiesDefaultState(CallbackInfo ci) {
        setDefaultState(getDefaultState()
            .with(Properties.LIT, false)
            .with(PROPERTIES.HAS_REDSTONE, false)
        );
    }

    @Unique @Nullable
    private PlayerEntity breaker;

    @Override
    public void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(Properties.LIT, PROPERTIES.HAS_REDSTONE);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean emitsRedstonePower(final BlockState state) {
        return super.emitsRedstonePower(state) || state.get(PROPERTIES.HAS_REDSTONE);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getWeakRedstonePower(final BlockState state, final BlockView world, final BlockPos pos, final Direction direction) {
        if (state.get(PROPERTIES.HAS_REDSTONE)) {
            return 15 - super.getWeakRedstonePower(state, world, pos, direction);
        } else {
            return super.getWeakRedstonePower(state, world, pos, direction);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onSyncedBlockEvent(final BlockState state, final World world, final BlockPos pos, final int type, final int data) {
        super.onSyncedBlockEvent(state, world, pos, type, data);

        final @Nullable BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity == null) {
            return false;
        }

        return blockEntity.onSyncedBlockEvent(type, data);
    }

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(final BlockState state, final World world, final BlockPos pos) {
        return (NamedScreenHandlerFactory) world.getBlockEntity(pos);
    }

    private void removeStack(final World world, final FrameBlockEntity from, final PlayerEntity to, final int slot, final boolean giveItem) {
        final ItemStack stack = from.removeStack(slot);
        if (!stack.isEmpty() && giveItem) {
            to.inventory.offerOrDrop(world, stack);

            world.playSound(null, from.getPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2f, (float) (Math.random() - Math.random()) * 1.4f + 2f);
        }
    }

    private void onHammerRemove(final World world, final FrameBlockEntity frame, final BlockState state, final PlayerEntity player, final boolean giveItem) {
        world.setBlockState(frame.getPos(), state);

        if (player.isSneaking()) {
            for (final int i : frame.sections().itemIndices()) {
                removeStack(world, frame, player, i, giveItem);
            }
        } else {
            int slot = -1;
            for (int i = frame.size() - 1; i >= 0; i--) {
                if (frame.items()[i].isPresent()) {
                    slot = i;
                    break;
                }
            }
            if (slot != -1) {
                removeStack(world, frame, player, slot, giveItem);
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStateReplaced(final BlockState oldState, final World world, final BlockPos pos, final BlockState newState, final boolean moved) {
        if (world.isClient || oldState.getBlock() == newState.getBlock()) {
            return;
        }

        final @Nullable BlockEntity blockEntity = world.getBlockEntity(pos);
        final @Nullable PlayerEntity player = breaker;

        // posToPlayer is only populated in creative mode, so don't give the item
        if (player != null) {
            if (player.getStackInHand(player.getActiveHand()).getItem() == ITEMS.FRAMERS_HAMMER) {
                //noinspection ConstantConditions // FrameBlock should always have a FrameEntity
                onHammerRemove(world, (FrameBlockEntity) blockEntity, oldState, player, false);
            } else {
                super.onStateReplaced(oldState, world, pos, newState, moved);
            }
        } else {
            if (blockEntity instanceof Inventory) {
                ItemScatterer.spawn(world, pos, (Inventory) blockEntity);
            }
            super.onStateReplaced(oldState, world, pos, newState, moved);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBlockBreakStart(final BlockState state, final World world, final BlockPos pos, final PlayerEntity player) {
        super.onBlockBreakStart(state, world, pos, player);

        if (world.isClient) {
            return;
        }

        if (player.getStackInHand(player.getActiveHand()).getItem() == ITEMS.FRAMERS_HAMMER) {
            final @Nullable BlockEntity blockEntity = world.getBlockEntity(pos);

            if (blockEntity instanceof FrameBlockEntity) {
                // onBlockBreakStart is not called in creative mode, so give the item
                onHammerRemove(world, (FrameBlockEntity) blockEntity, state, player, true);
            }
        }
    }

    @Override
    public void onBreak(final World world, final BlockPos pos, final BlockState state, final PlayerEntity player) {
        super.onBreak(world, pos, state, player);

        if (world.isClient || !player.isCreative()) {
            return;
        }

        breaker = player;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResult onUse(final BlockState state, final World world, final BlockPos pos, final PlayerEntity player, final Hand hand, final BlockHitResult hit) {
        final @Nullable BlockEntity blockEntity = world.getBlockEntity(pos);

        if (!(blockEntity instanceof FrameBlockEntity)) {
            return ActionResult.CONSUME;
        }

        final @Nonnull FrameBlockEntity frame = (FrameBlockEntity) blockEntity;

        final @Nullable ItemStack playerStack = player.getMainHandStack();

        if (playerStack != null) {
            final Vec3d posInBlock = hit.getPos().subtract(Vec3d.of(hit.getBlockPos()));
            final int relativeSlot = getRelativeSlotAt(posInBlock, hit.getSide());

            final Function3<List<Optional<ItemStack>>, Integer, Supplier<Unit>, ActionResult> swapItems = (slots, absoluteSlot, onSuccess) -> {
                final Optional<ItemStack> maybeStack = slots.get(relativeSlot);
                if (playerStack.getItem() != maybeStack.orElse(ItemStack.EMPTY).getItem()) {
                    if (!world.isClient) {
                        if (!player.isCreative() && maybeStack.isPresent()) {
                            player.inventory.offerOrDrop(world, maybeStack.get());
                        }
                        frame.copyFrom(absoluteSlot, playerStack, 1, !player.isCreative());
                        onSuccess.get();
                    }
                    return ActionResult.SUCCESS;
                } else {
                    return ActionResult.CONSUME;
                }
            };

            final ValidQuery.ItemStackValidQuery query = checkIf(playerStack);

            if (query.isValidForOverlay()) {
                final int absoluteSlot = frame.sections().overlay().makeAbsolute(relativeSlot);
                return swapItems.apply(frame.overlayItems(), absoluteSlot, () -> Unit.INSTANCE);
            }

            final Optional<BlockState> maybeBaseState = query.isValidForBase(i -> Optional.ofNullable(i.getBlock().getPlacementState(new ItemPlacementContext(new ItemUsageContext(player, hand, hit)))), world, pos);
            if (maybeBaseState.isPresent()) {
                final int absoluteSlot = frame.sections().base().makeAbsolute(relativeSlot);
                return swapItems.apply(frame.baseItems(), absoluteSlot, () -> {
                    frame.baseStates()[relativeSlot] = maybeBaseState;
                    return Unit.INSTANCE;
                });
            }

            if (query.isValidForSpecial()) {
                final SpecialItems.SpecialItem specialItem = SPECIAL_ITEMS.MAP.get(playerStack.getItem());
                final int slot = frame.sections().special().makeAbsolute(specialItem.offset());

                if (frame.getStack(slot).isEmpty()) {
                    if (!world.isClient) {
                        frame.copyFrom(slot, playerStack, 1, !player.isCreative());
                        specialItem.onAdd(world, frame);
                    }
                    return ActionResult.SUCCESS;
                } else {
                    return ActionResult.CONSUME;
                }
            }

            if (playerStack.isEmpty() && player.isSneaking()) {
                player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
                return ActionResult.SUCCESS;
            }
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public void onPlaced(final World world, final BlockPos pos, final BlockState state, @Nullable final LivingEntity placer, final ItemStack itemStack) {
        final boolean tryCopy;

        if (placer instanceof PlayerEntity) {
            final PlayerEntity player = (PlayerEntity) placer;
            if (player.getOffHandStack().getItem() != ITEMS.FRAMERS_HAMMER || player.getOffHandStack().getTag() == null) {
                tryCopy = false;
            } else {
                final BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof FrameBlockEntity) {
                    tryCopy = FramersHammer.Data.fromTag(player.getOffHandStack().getTag()).applySettings(this, state, (FrameBlockEntity) blockEntity, player, world);
                } else {
                    tryCopy = false;
                }
            }
        } else {
            tryCopy = false;
        }

        if (!tryCopy) {
            super.onPlaced(world, pos, state, placer, itemStack);
        }
    }
}
