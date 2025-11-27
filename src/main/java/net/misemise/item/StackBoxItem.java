package net.misemise.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.misemise.screen.StackBoxScreenHandler;

import java.util.List;

public class StackBoxItem extends Item {
    public static final int MAX_CAPACITY = 1_000_000;

    private static final String STORED_ITEM_KEY = "StoredItem";
    private static final String STORED_COUNT_KEY = "StoredCount";

    public StackBoxItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient()) {
            user.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, playerInventory, player) -> new StackBoxScreenHandler(syncId, playerInventory, stack),
                    Text.translatable("container.stackbox.stack_box")));
        }

        return ActionResult.SUCCESS;
    }

    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        NbtCompound nbt = getOrCreateNbt(stack);

        if (nbt.contains(STORED_ITEM_KEY)) {
            String itemId = nbt.getString(STORED_ITEM_KEY).orElse("");
            int count = nbt.getInt(STORED_COUNT_KEY).orElse(0);

            Identifier id = Identifier.tryParse(itemId);
            if (id != null && Registries.ITEM.containsId(id)) {
                Item storedItem = Registries.ITEM.get(id);
                ItemStack storedStack = new ItemStack(storedItem);

                tooltip.add(Text.translatable("item.stackbox.stack_box.stored",
                        storedStack.getName(), count).formatted(Formatting.GRAY));
                tooltip.add(Text.translatable("item.stackbox.stack_box.capacity",
                        count, MAX_CAPACITY).formatted(Formatting.DARK_GRAY));
            }
        } else {
            tooltip.add(Text.translatable("item.stackbox.stack_box.empty").formatted(Formatting.GRAY));
        }
    }

    public static NbtCompound getOrCreateNbt(ItemStack stack) {
        NbtComponent component = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        return component.copyNbt();
    }

    public static void saveNbt(ItemStack stack, NbtCompound nbt) {
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    public static String getStoredItemId(ItemStack stack) {
        NbtCompound nbt = getOrCreateNbt(stack);
        return nbt.getString(STORED_ITEM_KEY).orElse("");
    }

    public static int getStoredCount(ItemStack stack) {
        NbtCompound nbt = getOrCreateNbt(stack);
        return nbt.getInt(STORED_COUNT_KEY).orElse(0);
    }

    public static void setStoredItem(ItemStack stack, String itemId, int count) {
        NbtCompound nbt = getOrCreateNbt(stack);
        if (count > 0 || !itemId.isEmpty()) {
            nbt.putString(STORED_ITEM_KEY, itemId);
            nbt.putInt(STORED_COUNT_KEY, count);
        } else {
            nbt.remove(STORED_ITEM_KEY);
            nbt.remove(STORED_COUNT_KEY);
        }
        saveNbt(stack, nbt);
    }

    public static int addItems(ItemStack stack, String itemId, int count) {
        NbtCompound nbt = getOrCreateNbt(stack);
        String currentItemId = nbt.getString(STORED_ITEM_KEY).orElse("");
        int currentCount = nbt.getInt(STORED_COUNT_KEY).orElse(0);

        if (currentItemId.isEmpty() || currentItemId.equals(itemId)) {
            long newCount = (long) currentCount + count;
            if (newCount > MAX_CAPACITY) {
                setStoredItem(stack, itemId, MAX_CAPACITY);
                return (int) (newCount - MAX_CAPACITY);
            } else {
                setStoredItem(stack, itemId, (int) newCount);
                return 0;
            }
        }

        return count;
    }

    public static int removeItems(ItemStack stack, int count) {
        NbtCompound nbt = getOrCreateNbt(stack);
        int currentCount = nbt.getInt(STORED_COUNT_KEY).orElse(0);

        if (currentCount == 0) {
            return 0;
        }

        int toRemove = Math.min(count, currentCount);
        int remaining = currentCount - toRemove;

        String itemId = nbt.getString(STORED_ITEM_KEY).orElse("");
        setStoredItem(stack, itemId, remaining);

        return toRemove;
    }

    public static boolean isAutoCollectEnabled(ItemStack stack) {
        NbtCompound nbt = getOrCreateNbt(stack);
        if (!nbt.contains("AutoCollect")) {
            return true;
        }
        return nbt.getBoolean("AutoCollect").orElse(true);
    }

    public static void setAutoCollect(ItemStack stack, boolean enabled) {
        NbtCompound nbt = getOrCreateNbt(stack);
        nbt.putBoolean("AutoCollect", enabled);
        saveNbt(stack, nbt);
    }
}
