package dev.alexnader.framity2.mixin.local;

import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Unit;
import dev.alexnader.framity2.Framity2;
import dev.alexnader.framity2.block.FrameSlotInfo;
import dev.alexnader.framity2.block.entity.FrameBlockEntity;
import dev.alexnader.framity2.block.frame.*;
import dev.alexnader.framity2.items.FramersHammer;
import dev.alexnader.framity2.items.SpecialItems;
import dev.alexnader.framity2.util.ConstructorCallback;
import dev.alexnader.framity2.util.ValidQuery;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceGateBlock;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

import static dev.alexnader.framity2.Framity2.*;
import static dev.alexnader.framity2.util.ValidQuery.checkIf;

@Mixin({
    BlockFrame.class,
    SlabFrame.class,
    StairsFrame.class,
    FenceFrame.class,
    FenceGateBlock.class,
    TrapdoorFrame.class,
    DoorFrame.class,
    PathFrame.class,
    TorchFrame.class,
    WallTorchFrame.class
})
public abstract class FrameBehaviourMixin extends Block implements BlockEntityProvider, ConstructorCallback, Frame, FrameSlotInfo {
    public FrameBehaviourMixin(Settings settings) {
        super(settings);
        throw new IllegalStateException("Mixin constructor should never run.");
    }

    @Override
    public void onConstructor() {
        setDefaultState(getDefaultState()
            .with(Properties.LIT, false)
            .with(PROPERTIES.HAS_REDSTONE, false)
        );
    }

    @Unique
    private final Map<BlockPos, PlayerEntity> posToPlayer = new HashMap<>();

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(Properties.LIT, PROPERTIES.HAS_REDSTONE);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return state.get(PROPERTIES.HAS_REDSTONE);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(PROPERTIES.HAS_REDSTONE) ? 15 : 0;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
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
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return (NamedScreenHandlerFactory) world.getBlockEntity(pos);
    }

    private void removeStack(World world, FrameBlockEntity from, PlayerEntity to, int slot, boolean giveItem) {
        final ItemStack stack = from.removeStack(slot);
        if (!stack.isEmpty() && giveItem) {
            to.inventory.offerOrDrop(world, stack);

            world.playSound(null, from.getPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2f, (float) (Math.random() - Math.random()) * 1.4f + 2f);
        }
    }

    private void onHammerRemove(World world, FrameBlockEntity frame, BlockState state, PlayerEntity player, boolean giveItem) {
        world.setBlockState(frame.getPos(), state);

        if (player.isSneaking()) {
            for (final int i : frame.sections().itemIndices()) {
                removeStack(world, frame, player, i, giveItem);
            }
        } else {
            int slot = -1;
            for (int i = frame.size(); i >= 0; i--) {
                if (frame.items()[i].isPresent()) {
                    slot = i;
                    break;
                }
            }
            if (slot != -1) {
                removeStack(world, frame, player, slot, giveItem);
            }
        }

//        frameEntity.markDirty(); //TODO: determine if this is necessary
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStateReplaced(BlockState oldState, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (world.isClient || oldState.getBlock() == newState.getBlock()) {
            return;
        }

        final @Nullable BlockEntity blockEntity = world.getBlockEntity(pos);
        final @Nullable PlayerEntity player = posToPlayer.get(pos);

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
    public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
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
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);

        if (world.isClient || !player.isCreative()) {
            return;
        }

        posToPlayer.put(pos, player);
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        final @Nullable BlockEntity blockEntity = world.getBlockEntity(pos);

        if (!(blockEntity instanceof FrameBlockEntity)) {
            return ActionResult.CONSUME;
        }

        final @Nonnull FrameBlockEntity frame = (FrameBlockEntity) blockEntity;

        final @Nullable ItemStack playerStack = player.getMainHandStack();

        if (playerStack != null) {
            Vec3d posInBlock = hit.getPos().subtract(Vec3d.of(hit.getBlockPos()));
            int relativeSlot = getRelativeSlotAt(state, posInBlock, hit.getSide());

            Function3<List<Optional<ItemStack>>, Integer, Supplier<Unit>, ActionResult> swapItems = (slots, absoluteSlot, onSuccess) -> {
                Optional<ItemStack> maybeStack = slots.get(relativeSlot);
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

            ValidQuery.ItemStackValidQuery query = checkIf(playerStack);

            if (query.isValidForOverlay()) {
                int absoluteSlot = frame.sections().overlay().makeAbsolute(relativeSlot);
                return swapItems.apply(frame.overlayItems(), absoluteSlot, () -> Unit.INSTANCE);
            }

            Optional<BlockState> maybeBaseState = query.isValidForBase(i -> Optional.ofNullable(i.getBlock().getPlacementState(new ItemPlacementContext(new ItemUsageContext(player, hand, hit)))), world, pos);
            if (maybeBaseState.isPresent()) {
                int absoluteSlot = frame.sections().base().makeAbsolute(relativeSlot);
                swapItems.apply(frame.baseItems(), absoluteSlot, () -> {
                    frame.baseStates()[relativeSlot] = maybeBaseState;
                    return Unit.INSTANCE;
                });
            }

            if (query.isValidForSpecial()) {
                SpecialItems.SpecialItem specialItem = SPECIAL_ITEMS.MAP.get(playerStack.getItem());
                int slot = frame.sections().special().makeAbsolute(specialItem.offset());

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
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        boolean tryCopy;

        if (placer instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) placer;
            if (!(player.getOffHandStack().getItem() == ITEMS.FRAMERS_HAMMER) || player.getOffHandStack().getTag() == null) {
                tryCopy = false;
            } else {
                BlockEntity blockEntity = world.getBlockEntity(pos);
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
