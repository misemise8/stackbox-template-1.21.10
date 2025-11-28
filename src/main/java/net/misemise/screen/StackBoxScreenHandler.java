package net.misemise.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import net.misemise.item.StackBoxItem;

public class StackBoxScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final ItemStack stackBoxStack;
    private final PropertyDelegate propertyDelegate;

    public static final int BUTTON_DEPOSIT_ALL = 0;
    public static final int BUTTON_WITHDRAW_STACK = 1;
    public static final int BUTTON_FILL_INVENTORY = 2;
    public static final int BUTTON_TOGGLE_AUTO_COLLECT = 3;

    public StackBoxScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ItemStack.EMPTY);
    }

    public StackBoxScreenHandler(int syncId, PlayerInventory playerInventory, ItemStack stackBoxStack) {
        super(ModScreenHandlers.STACK_BOX_SCREEN_HANDLER, syncId);
        this.stackBoxStack = stackBoxStack;
        this.inventory = new SimpleInventory(1);

        // Custom PropertyDelegate to handle sync
        this.propertyDelegate = new PropertyDelegate() {
            private int clientCachedCount = 0;
            private int clientCachedItemId = 0;
            private int clientCachedAutoCollect = 1; // 1 = enabled by default

            @Override
            public int get(int index) {
                if (index == 0) {
                    // Count
                    if (!stackBoxStack.isEmpty()) {
                        return StackBoxItem.getStoredCount(stackBoxStack);
                    }
                    return clientCachedCount;
                } else if (index == 1) {
                    // Item ID (Raw)
                    if (!stackBoxStack.isEmpty()) {
                        String itemId = StackBoxItem.getStoredItemId(stackBoxStack);
                        if (!itemId.isEmpty()) {
                            Identifier id = Identifier.tryParse(itemId);
                            if (id != null && Registries.ITEM.containsId(id)) {
                                return Registries.ITEM.getRawId(Registries.ITEM.get(id));
                            }
                        }
                        return 0; // Air or invalid
                    }
                    return clientCachedItemId;
                } else if (index == 2) {
                    // Auto-collect enabled (0 or 1)
                    if (!stackBoxStack.isEmpty()) {
                        return StackBoxItem.isAutoCollectEnabled(stackBoxStack) ? 1 : 0;
                    }
                    return clientCachedAutoCollect;
                }
                return 0;
            }

            @Override
            public void set(int index, int value) {
                if (index == 0) {
                    clientCachedCount = value;
                } else if (index == 1) {
                    clientCachedItemId = value;
                } else if (index == 2) {
                    clientCachedAutoCollect = value;
                }
            }

            @Override
            public int size() {
                return 3;
            }
        };

        this.addProperties(propertyDelegate);

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

    public ItemStack getStackBoxStack() {
        return this.stackBoxStack;
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
                // Prevent StackBox from being shift-clicked into itself
                if (slotStack.getItem() instanceof StackBoxItem) {
                    return ItemStack.EMPTY;
                }
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
            case BUTTON_TOGGLE_AUTO_COLLECT:
                return handleToggleAutoCollect(player);
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

        int maxStackSize = Registries.ITEM.get(id).getMaxCount();
        int toWithdraw = Math.min(maxStackSize, StackBoxItem.getStoredCount(stackBoxStack));

        if (toWithdraw == 0) {
            return false;
        }

        ItemStack withdrawStack = new ItemStack(Registries.ITEM.get(id), toWithdraw);
        int originalCount = withdrawStack.getCount();

        // Try to insert into player inventory
        // insertStack modifies the passed stack, reducing its count by the amount
        // inserted
        player.getInventory().insertStack(withdrawStack);

        // Explicitly mark inventory dirty to ensure client update
        player.getInventory().markDirty();

        // Calculate how many were actually inserted
        int remaining = withdrawStack.getCount();
        int inserted = originalCount - remaining;

        if (inserted > 0) {
            StackBoxItem.removeItems(stackBoxStack, inserted);
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

        Item item = Registries.ITEM.get(id);
        int maxStackSize = item.getMaxCount();
        boolean changed = false;

        // 1. Top off existing stacks
        for (int i = 1; i < this.slots.size(); i++) {
            Slot slot = this.slots.get(i);
            ItemStack stack = slot.getStack();

            if (!stack.isEmpty() && stack.getItem() == item) {
                int currentCount = stack.getCount();
                if (currentCount < maxStackSize) {
                    int needed = maxStackSize - currentCount;
                    int available = StackBoxItem.getStoredCount(stackBoxStack);
                    int toAdd = Math.min(needed, available);

                    if (toAdd > 0) {
                        stack.increment(toAdd);
                        StackBoxItem.removeItems(stackBoxStack, toAdd);
                        slot.markDirty();
                        changed = true;
                    }
                }
            }
        }

        // 2. Fill empty slots
        for (int i = 1; i < this.slots.size(); i++) {
            Slot slot = this.slots.get(i);
            if (!slot.hasStack()) {
                int available = StackBoxItem.getStoredCount(stackBoxStack);
                if (available == 0) {
                    break;
                }

                int toAdd = Math.min(maxStackSize, available);
                ItemStack newStack = new ItemStack(item, toAdd);
                slot.setStack(newStack);
                StackBoxItem.removeItems(stackBoxStack, toAdd);
                slot.markDirty();
                changed = true;
            }
        }

        return changed;
    }

    private boolean handleToggleAutoCollect(PlayerEntity player) {
        if (stackBoxStack.isEmpty()) {
            return false;
        }

        boolean currentState = StackBoxItem.isAutoCollectEnabled(stackBoxStack);
        StackBoxItem.setAutoCollect(stackBoxStack, !currentState);
        return true;
    }

    public int getStoredCount() {
        return this.propertyDelegate.get(0);
    }

    public String getStoredItemId() {
        if (!stackBoxStack.isEmpty()) {
            return StackBoxItem.getStoredItemId(stackBoxStack);
        }
        // Fallback for client using synced ID
        int rawId = this.propertyDelegate.get(1);
        if (rawId != 0) {
            return Registries.ITEM.getId(Registries.ITEM.get(rawId)).toString();
        }
        return "";
    }

    public boolean getAutoCollectEnabled() {
        return this.propertyDelegate.get(2) == 1;
    }

}