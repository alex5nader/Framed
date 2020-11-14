package dev.alexnader.framity2.items;

import com.mojang.datafixers.util.Pair;
import dev.alexnader.framity2.block.FrameSlotInfo;
import dev.alexnader.framity2.block.entity.FrameBlockEntity;
import dev.alexnader.framity2.block.frame.data.FrameData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.ModelPredicateProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class FramersHammer extends Item {
    public FramersHammer(final Settings settings) {
        super(settings);
    }

    public enum CopyMode {
        NONE(0, "none"), ANY(1, "any"), REQUIRE_ALL(2, "require_all");

        public final int id;
        public final String translationKey;

        CopyMode(final int id, final String translationKey) {
            this.id = id;
            this.translationKey = translationKey;
        }

        public CopyMode next() {
            return CopyMode.values()[(id + 1) % CopyMode.values().length];
        }

        public static final CopyMode DEFAULT = NONE;

        public static Optional<CopyMode> fromString(final String string) {
            return Arrays.stream(values()).filter(s -> s.translationKey.equals(string)).findFirst();
        }

        public static CopyMode fromStringOrDefault(final String string) {
            return fromString(string).orElse(DEFAULT);
        }
    }

    public static class Data {
        public static Data fromTag(final CompoundTag tag) {
            return new Data(
                tag.contains("storedData")
                    ? FrameData.fromTag(tag.getCompound("storedData"))
                    : null,
                (tag.contains("mode")
                    ? CopyMode.fromString(tag.getString("mode"))
                    : Optional.<CopyMode>empty()
                ).orElse(CopyMode.DEFAULT)
            );
        }

        private final @Nullable
        FrameData storedData;
        private final CopyMode mode;

        public Data(@Nullable final FrameData storedData, final CopyMode mode) {
            this.storedData = storedData;
            this.mode = mode;
        }

        public boolean applySettings(final FrameSlotInfo slotInfo, final BlockState state, final FrameBlockEntity frame, final PlayerEntity player, final World world) {
            final FrameData storedData = this.storedData;
            if (storedData == null) {
                return false;
            }

            if (!storedData.sections().equals(frame.sections())) {
                player.sendMessage(new TranslatableText("gui.framity.framers_hammer.different_format"), true);
                return false;
            }

            if (player.isCreative()) {
                if (mode == CopyMode.NONE) {
                    return false;
                }

                if (!world.isClient) {
                    IntStream.range(0, storedData.items().length)
                        .boxed()
                        .map(i -> new Pair<>(storedData.items()[i], i))
                        .flatMap(pair -> pair.getFirst().map(itemStack -> Stream.of(new Pair<>(itemStack, pair.getSecond()))).orElseGet(Stream::empty))
                        .forEach(pair -> frame.setStack(pair.getSecond(), pair.getFirst().copy()));
                }
            } else {
                final boolean requireAllItems;
                switch (mode) {
                case NONE:
                    return false;
                case ANY:
                    requireAllItems = false;
                    break;
                case REQUIRE_ALL:
                    requireAllItems = true;
                    break;
                default:
                    throw new IllegalStateException("Unreachable.");
                }

                final Map<Item, Integer> itemSlotToFrameSlot = IntStream.range(0, storedData.items().length)
                    .boxed()
                    .map(i -> new Pair<>(storedData.items()[i], i))
                    .flatMap(pair -> {
                        if (pair.getFirst().isPresent()) {
                            //noinspection OptionalGetWithoutIsPresent
                            return Stream.of(pair.mapFirst(stack -> stack.get().getItem()));
                        } else {
                            return Stream.empty();
                        }
                    })
                    .collect(Pair.toMap());

                final Map<Integer, Integer> playerSlotToFrameSlot = IntStream.range(0, player.inventory.size())
                    .boxed()
                    .map(i -> new Pair<>(Optional.of(player.inventory.getStack(i)).filter(s -> !s.isEmpty()), i))
                    .flatMap(pair -> {
                        final Optional<ItemStack> maybeStack = pair.getFirst();
                        if (maybeStack.isPresent()) {
                            final Item item = maybeStack.get().getItem();
                            if (itemSlotToFrameSlot.containsKey(item)) {
                                return Stream.of(new Pair<>(pair.getSecond(), itemSlotToFrameSlot.get(item)));
                            } else {
                                return Stream.empty();
                            }
                        } else {
                            return Stream.empty();
                        }
                    })
                    .collect(Pair.toMap());

                if (requireAllItems && playerSlotToFrameSlot.size() != Arrays.stream(storedData.items()).filter(Optional::isPresent).count()) {
                    return false;
                }

                if (!world.isClient) {
                    for (final Map.Entry<Integer, Integer> entry : playerSlotToFrameSlot.entrySet()) {
                        final int playerSlot = entry.getKey();
                        final int frameSlot = entry.getValue();

                        if (frame.getStack(frameSlot).getItem() != player.inventory.getStack(playerSlot).getItem() && slotInfo.absoluteSlotIsValid(frame, state, frameSlot)) {
                            if (!frame.getStack(frameSlot).isEmpty()) {
                                player.inventory.offerOrDrop(world, frame.removeStack(frameSlot));
                            }
                            frame.setStack(frameSlot, player.inventory.removeStack(playerSlot, 1));
                        }
                    }
                }
            }

            player.sendMessage(new TranslatableText("gui.framity.framers_hammer.apply_settings"), true);

            return true;
        }
    }

    public static final ModelPredicateProvider MODEL_PREDICATE = (stack, world, entity) ->
        Optional.ofNullable(stack.getTag())
            .map(t -> t.getString("mode"))
            .flatMap(CopyMode::fromString)
            .orElse(CopyMode.DEFAULT)
            .id;

    private CompoundTag getTagOrAssignDefault(final ItemStack stack) {
        if (stack.getTag() == null) {
            final CompoundTag tag = new CompoundTag();
            tag.putString("mode", CopyMode.DEFAULT.toString());
            stack.setTag(tag);
        }
        return stack.getTag();
    }

    @Override
    public ActionResult useOnBlock(final ItemUsageContext context) {
        final CompoundTag tag = getTagOrAssignDefault(context.getStack());
        final Data data = Data.fromTag(tag);

        final BlockPos pos = context.getBlockPos();
        final World world = context.getWorld();
        final BlockState state = world.getBlockState(pos);

        final Block block = state.getBlock();
        if (!(block instanceof FrameSlotInfo)) {
            return super.useOnBlock(context);
        }
        final FrameSlotInfo slotInfo = (FrameSlotInfo) block;

        final BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof FrameBlockEntity)) {
            return super.useOnBlock(context);
        }
        final FrameBlockEntity frame = (FrameBlockEntity) blockEntity;

        final PlayerEntity player = context.getPlayer();
        if (player == null) {
            return super.useOnBlock(context);
        }

        if (player.isSneaking()) {
            player.sendMessage(new TranslatableText("gui.framity.framers_hammer.copy_settings"), true);
            tag.put("storedData", frame.data.toTag());

            return ActionResult.SUCCESS;
        } else {
            if (data.applySettings(slotInfo, state, frame, player, world)) {
                return ActionResult.SUCCESS;
            } else {
                return super.useOnBlock(context);
            }
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(final World world, final PlayerEntity user, final Hand hand) {
        if (!user.isSneaking()) {
            return super.use(world, user, hand);
        }

        final ItemStack stack = user.getStackInHand(hand);
        final CompoundTag tag = getTagOrAssignDefault(stack);
        final CopyMode mode = CopyMode.fromStringOrDefault(tag.getString("mode"));

        final CopyMode newMode = mode.next();
        tag.putString("mode", newMode.toString());
        user.sendMessage(new TranslatableText("gui.framity.framers_hammer." + newMode.translationKey), true);

        return TypedActionResult.success(stack);
    }
}
