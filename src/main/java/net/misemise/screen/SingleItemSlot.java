package net.misemise.screen;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.misemise.item.StackBoxItem;

/**
 * Custom slot that only accepts items of the same type as already stored.
 * This ensures the Stack Box can only contain one type of item.
 */
public class SingleItemSlot extends Slot {
    private final ItemStack stackBoxStack;

    public SingleItemSlot(Inventory inventory, int index, int x, int y, ItemStack stackBoxStack) {
        super(inventory, index, x, y);
        this.stackBoxStack = stackBoxStack;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        // Prevent StackBox from being inserted into itself
        if (stack.getItem() instanceof StackBoxItem) {
            return false;
        }

        // If Stack Box is empty, allow any item
        String storedItemId = StackBoxItem.getStoredItemId(stackBoxStack);
        if (storedItemId.isEmpty()) {
            return true;
        }

        // Only allow items of the same type
        String insertItemId = Registries.ITEM.getId(stack.getItem()).toString();
        return insertItemId.equals(storedItemId);
    }

    @Override
    public int getMaxItemCount() {
        // Only show 1 item in the slot (actual count is stored in NBT)
        return 1;
    }

    @Override
    public void setStack(ItemStack stack) {
        ItemStack previousStack = this.getStack().copy();
        super.setStack(stack);

        if (stackBoxStack.isEmpty()) {
            return;
        }

        // When an item is placed in the slot
        if (!stack.isEmpty()) {
            String itemId = Registries.ITEM.getId(stack.getItem()).toString();
            String currentItemId = StackBoxItem.getStoredItemId(stackBoxStack);

            // If this is a new item type, initialize it
            if (currentItemId.isEmpty()) {
                StackBoxItem.setStoredItem(stackBoxStack, itemId, 0);
            }

            // If a stack was inserted (not just moving the display item)
            if (previousStack.isEmpty() && stack.getCount() == 1) {
                // This is just the display item, don't add to count
            }
        }
    }

    @Override
    public ItemStack insertStack(ItemStack stack, int count) {
        if (stackBoxStack.isEmpty() || stack.isEmpty()) {
            return stack;
        }

        // Prevent StackBox from being inserted
        if (stack.getItem() instanceof StackBoxItem) {
            return stack;
        }

        String itemId = Registries.ITEM.getId(stack.getItem()).toString();
        String storedItemId = StackBoxItem.getStoredItemId(stackBoxStack);

        // If empty or same item type
        if (storedItemId.isEmpty() || storedItemId.equals(itemId)) {
            int overflow = StackBoxItem.addItems(stackBoxStack, itemId, count);

            // Set display item if not already set
            if (this.getStack().isEmpty()) {
                super.setStack(new ItemStack(stack.getItem(), 1));
            }

            // Return overflow items
            if (overflow > 0) {
                ItemStack overflowStack = stack.copy();
                overflowStack.setCount(overflow);
                return overflowStack;
            }
            return ItemStack.EMPTY;
        }

        return stack;
    }

    @Override
    public ItemStack takeStack(int amount) {
        if (stackBoxStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        String storedItemId = StackBoxItem.getStoredItemId(stackBoxStack);
        int storedCount = StackBoxItem.getStoredCount(stackBoxStack);

        if (storedItemId.isEmpty() || storedCount == 0) {
            super.setStack(ItemStack.EMPTY);
            return ItemStack.EMPTY;
        }

        int toRemove = Math.min(amount, storedCount);
        int actuallyRemoved = StackBoxItem.removeItems(stackBoxStack, toRemove);

        if (actuallyRemoved > 0) {
            ItemStack result = new ItemStack(Registries.ITEM.get(
                    net.minecraft.util.Identifier.tryParse(storedItemId)), actuallyRemoved);

            // If storage is now empty, clear the display item
            if (StackBoxItem.getStoredCount(stackBoxStack) == 0) {
                super.setStack(ItemStack.EMPTY);
            }

            return result;
        }

        return ItemStack.EMPTY;
    }
}