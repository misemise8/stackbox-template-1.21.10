package net.misemise.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import net.misemise.item.StackBoxItem;

/**
 * Screen handler for the Stack Box GUI.
 * Manages the inventory slots, player interaction, and button actions.
 */
public class StackBoxScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final ItemStack stackBoxStack;

    // Button action IDs
    public static final int BUTTON_DEPOSIT_ALL = 0;
    public static final int BUTTON_WITHDRAW_STACK = 1;
    public static final int BUTTON_FILL_INVENTORY = 2;

    public StackBoxScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ItemStack.EMPTY);
    }

    public StackBoxScreenHandler(int syncId, PlayerInventory playerInventory, ItemStack stackBoxStack) {
        super(ModScreenHandlers.STACK_BOX_SCREEN_HANDLER, syncId);
        this.stackBoxStack = stackBoxStack;
        this.inventory = new SimpleInventory(1);

        // Load stored item into the slot
        loadStoredItem();

        // Add the single storage slot (centered at x=80)
        this.addSlot(new SingleItemSlot(inventory, 0, 80, 20, stackBoxStack));

        // Add player inventory slots
        int i;
        // Player inventory (3 rows of 9)
        for (i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 51 + i * 18));
            }
        }

        // Player hotbar
        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 109));
        }
    }

    /**
     * Load the stored item from NBT into the inventory slot
     */
    private void loadStoredItem() {
        if (!stackBoxStack.isEmpty()) {
            String itemId = StackBoxItem.getStoredItemId(stackBoxStack);
            if (!itemId.isEmpty()) {
                Identifier id = Identifier.tryParse(itemId);
                if (id != null && Registries.ITEM.containsId(id)) {
                    ItemStack storedStack = new ItemStack(Registries.ITEM.get(id), 1);
                    inventory.setStack(0, storedStack);
                }
            }
        }
    }

    /**
     * Save the current slot item back to NBT
     */
    private void saveStoredItem() {
        if (!stackBoxStack.isEmpty()) {
            ItemStack slotStack = inventory.getStack(0);
            String currentItemId = StackBoxItem.getStoredItemId(stackBoxStack);
            int currentCount = StackBoxItem.getStoredCount(stackBoxStack);

            if (slotStack.isEmpty() && currentCount == 0) {
                // Clear storage
                StackBoxItem.setStoredItem(stackBoxStack, "", 0);
            } else if (!slotStack.isEmpty()) {
                // Ensure the item ID is saved
                String itemId = Registries.ITEM.getId(slotStack.getItem()).toString();
                if (currentItemId.isEmpty()) {
                    StackBoxItem.setStoredItem(stackBoxStack, itemId, 0);
                }
            }
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        saveStoredItem();
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot clickedSlot = this.slots.get(slot);

        if (clickedSlot != null && clickedSlot.hasStack()) {
            ItemStack slotStack = clickedSlot.getStack();
            newStack = slotStack.copy();

            if (slot == 0) {
                // Moving from storage slot to player inventory - not allowed for shift-click
                return ItemStack.EMPTY;
            } else {
                // Moving from player inventory to storage slot
                if (!this.insertItem(slotStack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                clickedSlot.setStack(ItemStack.EMPTY);
            } else {
                clickedSlot.markDirty();
            }
        }

        return newStack;
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (stackBoxStack.isEmpty()) {
            return false;
        }

        switch (id) {
            case BUTTON_DEPOSIT_ALL:
                return handleDepositAll(player);
            case BUTTON_WITHDRAW_STACK:
                return handleWithdrawStack(player);
            case BUTTON_FILL_INVENTORY:
                return handleFillInventory(player);
            default:
                return false;
        }
    }

    /**
     * Deposit all matching items from player inventory into the Stack Box
     */
    private boolean handleDepositAll(PlayerEntity player) {
        String storedItemId = StackBoxItem.getStoredItemId(stackBoxStack);

        // If no item is stored yet, check the slot
        if (storedItemId.isEmpty()) {
            ItemStack slotStack = inventory.getStack(0);
            if (!slotStack.isEmpty()) {
                storedItemId = Registries.ITEM.getId(slotStack.getItem()).toString();
                StackBoxItem.setStoredItem(stackBoxStack, storedItemId, 0);
            } else {
                return false; // Nothing to deposit
            }
        }

        Identifier targetId = Identifier.tryParse(storedItemId);
        if (targetId == null) {
            return false;
        }

        int totalDeposited = 0;

        // Iterate through player inventory (skip the storage slot at index 0)
        for (int i = 1; i < this.slots.size(); i++) {
            Slot slot = this.slots.get(i);
            ItemStack stack = slot.getStack();

            if (!stack.isEmpty() && Registries.ITEM.getId(stack.getItem()).equals(targetId)) {
                int count = stack.getCount();
                int overflow = StackBoxItem.addItems(stackBoxStack, storedItemId, count);
                int deposited = count - overflow;

                if (deposited > 0) {
                    stack.decrement(deposited);
                    totalDeposited += deposited;
                    slot.markDirty();
                }

                if (overflow > 0) {
                    // Stack Box is full
                    break;
                }
            }
        }

        return totalDeposited > 0;
    }

    /**
     * Withdraw one stack (64 items) from the Stack Box
     */
    private boolean handleWithdrawStack(PlayerEntity player) {
        String storedItemId = StackBoxItem.getStoredItemId(stackBoxStack);
        if (storedItemId.isEmpty()) {
            return false;
        }

        Identifier id = Identifier.tryParse(storedItemId);
        if (id == null || !Registries.ITEM.containsId(id)) {
            return false;
        }

        int toWithdraw = Math.min(64, StackBoxItem.getStoredCount(stackBoxStack));
        if (toWithdraw == 0) {
            return false;
        }

        ItemStack withdrawStack = new ItemStack(Registries.ITEM.get(id), toWithdraw);

        // Try to add to player inventory
        if (player.getInventory().insertStack(withdrawStack)) {
            StackBoxItem.removeItems(stackBoxStack, toWithdraw);
            return true;
        } else if (withdrawStack.getCount() < toWithdraw) {
            // Partially added
            int actuallyWithdrawn = toWithdraw - withdrawStack.getCount();
            StackBoxItem.removeItems(stackBoxStack, actuallyWithdrawn);
            return true;
        }

        return false;
    }

    /**
     * Fill player inventory with items from the Stack Box
     */
    private boolean handleFillInventory(PlayerEntity player) {
        String storedItemId = StackBoxItem.getStoredItemId(stackBoxStack);
        if (storedItemId.isEmpty()) {
            return false;
        }

        Identifier id = Identifier.tryParse(storedItemId);
        if (id == null || !Registries.ITEM.containsId(id)) {
            return false;
        }

        int totalWithdrawn = 0;
        int maxStackSize = Registries.ITEM.get(id).getMaxCount();

        // First, top off existing stacks
        for (int i = 1; i < this.slots.size(); i++) {
            Slot slot = this.slots.get(i);
            ItemStack stack = slot.getStack();

            if (!stack.isEmpty() && Registries.ITEM.getId(stack.getItem()).equals(id)) {
                int currentCount = stack.getCount();
                if (currentCount < maxStackSize) {
                    int needed = maxStackSize - currentCount;
                    int available = StackBoxItem.getStoredCount(stackBoxStack);
                    int toAdd = Math.min(needed, available);

                    if (toAdd > 0) {
                        stack.increment(toAdd);
                        StackBoxItem.removeItems(stackBoxStack, toAdd);
                        totalWithdrawn += toAdd;
                        slot.markDirty();
                    }
                }
            }
        }

        // Then, fill empty slots
        for (int i = 1; i < this.slots.size(); i++) {
            Slot slot = this.slots.get(i);
            ItemStack stack = slot.getStack();

            if (stack.isEmpty()) {
                int available = StackBoxItem.getStoredCount(stackBoxStack);
                int toAdd = Math.min(maxStackSize, available);

                if (toAdd > 0) {
                    ItemStack newStack = new ItemStack(Registries.ITEM.get(id), toAdd);
                    slot.setStack(newStack);
                    StackBoxItem.removeItems(stackBoxStack, toAdd);
                    totalWithdrawn += toAdd;
                    slot.markDirty();
                }
            }
        }

        return totalWithdrawn > 0;
    }

    /**
     * Get the stored item count for display
     */
    public int getStoredCount() {
        return stackBoxStack.isEmpty() ? 0 : StackBoxItem.getStoredCount(stackBoxStack);
    }

    /**
     * Get the stored item ID for display
     */
    public String getStoredItemId() {
        return stackBoxStack.isEmpty() ? "" : StackBoxItem.getStoredItemId(stackBoxStack);
    }
}
