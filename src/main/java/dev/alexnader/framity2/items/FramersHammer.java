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
    public FramersHammer() {
        super(new Item.Settings().maxCount(1));
    }

    public enum CopyMode {
        NONE(0, "none"), ANY(1, "any"), REQUIRE_ALL(2, "require_all");

        public final int id;
        public final String translationKey;

        CopyMode(int id, String translationKey) {
            this.id = id;
            this.translationKey = translationKey;
        }

        public CopyMode next() {
            return CopyMode.values()[(id + 1) % CopyMode.values().length];
        }

        public static final CopyMode DEFAULT = NONE;

        public static Optional<CopyMode> fromString(String string) {
            return Arrays.stream(values()).filter(s -> s.translationKey.equals(string)).findFirst();
        }

        public static CopyMode fromStringOrDefault(String string) {
            return fromString(string).orElse(DEFAULT);
        }
    }

    public static class Data {
        public static Data fromTag(CompoundTag tag) {
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

        public Data(@Nullable FrameData storedData, CopyMode mode) {
            this.storedData = storedData;
            this.mode = mode;
        }

        @Nullable
        public FrameData storedData() {
            return storedData;
        }

        public CopyMode mode() {
            return mode;
        }

        public boolean applySettings(FrameSlotInfo slotInfo, BlockState state, FrameBlockEntity frame, PlayerEntity player, World world) {
            FrameData storedData = this.storedData;
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
                boolean requireAllItems;
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

                Map<Item, Integer> itemSlotToFrameSlot = IntStream.range(0, storedData.items().length)
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

                Map<Integer, Integer> playerSlotToFrameSlot = IntStream.range(0, player.inventory.size())
                    .boxed()
                    .map(i -> new Pair<>(Optional.of(player.inventory.getStack(i)).filter(s -> !s.isEmpty()), i))
                    .flatMap(pair -> {
                        Optional<ItemStack> maybeStack = pair.getFirst();
                        if (maybeStack.isPresent()) {
                            Item item = maybeStack.get().getItem();
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
                    for (Map.Entry<Integer, Integer> entry : playerSlotToFrameSlot.entrySet()) {
                        int playerSlot = entry.getKey();
                        int frameSlot = entry.getValue();

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

    private CompoundTag getTagOrAssignDefault(ItemStack stack) {
        if (stack.getTag() == null) {
            CompoundTag tag = new CompoundTag();
            tag.putString("mode", CopyMode.DEFAULT.toString());
            stack.setTag(tag);
        }
        return stack.getTag();
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        CompoundTag tag = getTagOrAssignDefault(context.getStack());
        Data data = Data.fromTag(tag);

        BlockPos pos = context.getBlockPos();
        World world = context.getWorld();
        BlockState state = world.getBlockState(pos);

        Block block = state.getBlock();
        if (!(block instanceof FrameSlotInfo)) {
            return super.useOnBlock(context);
        }
        FrameSlotInfo slotInfo = (FrameSlotInfo) block;

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof FrameBlockEntity)) {
            return super.useOnBlock(context);
        }
        FrameBlockEntity frame = (FrameBlockEntity) blockEntity;

        PlayerEntity player = context.getPlayer();
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
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!user.isSneaking()) {
            return super.use(world, user, hand);
        }

        ItemStack stack = user.getStackInHand(hand);
        CompoundTag tag = getTagOrAssignDefault(stack);
        CopyMode mode = CopyMode.fromStringOrDefault(tag.getString("mode"));

        CopyMode newMode = mode.next();
        tag.putString("mode", newMode.toString());
        user.sendMessage(new TranslatableText("gui.framity.framers_hammer." + newMode.translationKey), true);

        return TypedActionResult.success(stack);
    }
}
