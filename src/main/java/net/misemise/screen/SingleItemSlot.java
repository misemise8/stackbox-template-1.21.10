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
        super.setStack(stack);

        // When an item is placed in the slot, update the Stack Box NBT
        if (!stackBoxStack.isEmpty() && !stack.isEmpty()) {
            String itemId = Registries.ITEM.getId(stack.getItem()).toString();
            String currentItemId = StackBoxItem.getStoredItemId(stackBoxStack);

            // If this is a new item type, initialize it
            if (currentItemId.isEmpty()) {
                StackBoxItem.setStoredItem(stackBoxStack, itemId, 0);
            }
        }
    }
}
