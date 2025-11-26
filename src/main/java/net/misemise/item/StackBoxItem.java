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

/**
 * Stack Box Item - A backpack-like item that can store large quantities of a
 * single item type.
 * 
 * Features:
 * - Stores up to 1,000,000 of a single item type
 * - Opens a GUI for interaction
 * - Uses NBT data for persistent storage
 * - Extensible design for future features
 */
public class StackBoxItem extends Item {
    public static final int MAX_CAPACITY = 1_000_000;

    // NBT keys for data storage
    private static final String STORED_ITEM_KEY = "StoredItem";
    private static final String STORED_COUNT_KEY = "StoredCount";

    public StackBoxItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient()) {
            // Open the GUI on the server side
            user.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, playerInventory, player) -> new StackBoxScreenHandler(syncId, playerInventory, stack),
                    Text.translatable("container.stackbox.stack_box")));
        }

        return ActionResult.SUCCESS;
    }

    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        NbtCompound nbt = getOrCreateNbt(stack);

        if (nbt.contains(STORED_ITEM_KEY)) {
            String itemId = nbt.getString(STORED_ITEM_KEY).orElse("");
            int count = nbt.getInt(STORED_COUNT_KEY).orElse(0);

            // Get the stored item for display
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

    /**
     * Get or create NBT data for the stack
     */
    public static NbtCompound getOrCreateNbt(ItemStack stack) {
        NbtComponent component = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        return component.copyNbt();
    }

    /**
     * Save NBT data to the stack
     */
    public static void saveNbt(ItemStack stack, NbtCompound nbt) {
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    /**
     * Get the stored item identifier
     */
    public static String getStoredItemId(ItemStack stack) {
        NbtCompound nbt = getOrCreateNbt(stack);
        return nbt.getString(STORED_ITEM_KEY).orElse("");
    }

    /**
     * Get the stored item count
     */
    public static int getStoredCount(ItemStack stack) {
        NbtCompound nbt = getOrCreateNbt(stack);
        return nbt.getInt(STORED_COUNT_KEY).orElse(0);
    }

    /**
     * Set the stored item and count
     */
    public static void setStoredItem(ItemStack stack, String itemId, int count) {
        NbtCompound nbt = getOrCreateNbt(stack);
        if (count > 0) {
            nbt.putString(STORED_ITEM_KEY, itemId);
            nbt.putInt(STORED_COUNT_KEY, count);
        } else {
            // Clear storage if count is 0
            nbt.remove(STORED_ITEM_KEY);
            nbt.remove(STORED_COUNT_KEY);
        }
        saveNbt(stack, nbt);
    }

    /**
     * Add items to storage
     * 
     * @return The number of items that couldn't be added (overflow)
     */
    public static int addItems(ItemStack stack, String itemId, int count) {
        NbtCompound nbt = getOrCreateNbt(stack);
        String currentItemId = nbt.getString(STORED_ITEM_KEY).orElse("");
        int currentCount = nbt.getInt(STORED_COUNT_KEY).orElse(0);

        // If empty or same item type
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

        // Different item type, can't add
        return count;
    }

    /**
     * Remove items from storage
     * 
     * @return The actual number of items removed
     */
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
}
