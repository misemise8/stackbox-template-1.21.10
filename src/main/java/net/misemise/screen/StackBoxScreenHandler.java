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

public class StackBoxScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final ItemStack stackBoxStack;

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

        loadStoredItem();

        // Add the single storage slot (centered at top)
        this.addSlot(new SingleItemSlot(inventory, 0, 80, 20, stackBoxStack));

        // Add player inventory slots (3 rows of 9)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Add player hotbar (1 row of 9)
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

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

    private void saveStoredItem() {
        if (!stackBoxStack.isEmpty()) {
            ItemStack slotStack = inventory.getStack(0);
            String currentItemId = StackBoxItem.getStoredItemId(stackBoxStack);
            int currentCount = StackBoxItem.getStoredCount(stackBoxStack);

            if (slotStack.isEmpty() && currentCount == 0) {
                StackBoxItem.setStoredItem(stackBoxStack, "", 0);
            } else if (!slotStack.isEmpty()) {
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
                // Moving from storage slot - not allowed
                return ItemStack.EMPTY;
            } else {
                // Moving from player inventory to storage
                String itemId = Registries.ITEM.getId(slotStack.getItem()).toString();
                int count = slotStack.getCount();

                // Try to add to Stack Box
                int overflow = StackBoxItem.addItems(stackBoxStack, itemId, count);

                if (overflow < count) {
                    // Some items were added
                    if (overflow > 0) {
                        slotStack.setCount(overflow);
                    } else {
                        slotStack.setCount(0);
                    }

                    // Update display item in slot 0
                    ItemStack displayStack = inventory.getStack(0);
                    if (displayStack.isEmpty()) {
                        inventory.setStack(0, new ItemStack(slotStack.getItem(), 1));
                    }

                    clickedSlot.markDirty();
                    return newStack;
                }

                return ItemStack.EMPTY;
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

    private boolean handleDepositAll(PlayerEntity player) {
        String storedItemId = StackBoxItem.getStoredItemId(stackBoxStack);

        if (storedItemId.isEmpty()) {
            ItemStack slotStack = inventory.getStack(0);
            if (!slotStack.isEmpty()) {
                storedItemId = Registries.ITEM.getId(slotStack.getItem()).toString();
                StackBoxItem.setStoredItem(stackBoxStack, storedItemId, 0);
            } else {
                return false;
            }
        }

        Identifier targetId = Identifier.tryParse(storedItemId);
        if (targetId == null) {
            return false;
        }

        int totalDeposited = 0;

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
                    break;
                }
            }
        }

        return totalDeposited > 0;
    }

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

        if (player.getInventory().insertStack(withdrawStack)) {
            StackBoxItem.removeItems(stackBoxStack, toWithdraw);
            return true;
        } else if (withdrawStack.getCount() < toWithdraw) {
            int actuallyWithdrawn = toWithdraw - withdrawStack.getCount();
            StackBoxItem.removeItems(stackBoxStack, actuallyWithdrawn);
            return true;
        }

        return false;
    }

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

        // Top off existing stacks
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

        // Fill empty slots
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

    public int getStoredCount() {
        return stackBoxStack.isEmpty() ? 0 : StackBoxItem.getStoredCount(stackBoxStack);
    }

    public String getStoredItemId() {
        return stackBoxStack.isEmpty() ? "" : StackBoxItem.getStoredItemId(stackBoxStack);
    }
}