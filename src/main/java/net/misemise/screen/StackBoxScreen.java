package net.misemise.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.registry.Registries;
import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.glfw.GLFW;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.misemise.network.ModMessages;
import net.misemise.item.StackBoxItem;

public class StackBoxScreen extends HandledScreen<StackBoxScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of("stackbox", "textures/gui/stack_box.png");
    private static final int TEXTURE_WIDTH = 176;
    private static final int TEXTURE_HEIGHT = 166;

    private ButtonWidget autoCollectButton;
    private int currentTabSlotIndex = -1;

    // Static variables to preserve cursor position across screen re-opens
    private static double lastMouseX = -1;
    private static double lastMouseY = -1;

    // Static variable to track tab switches
    public static int pendingTabSlotIndex = -1;

    private boolean firstRender = true;

    public StackBoxScreen(StackBoxScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 166;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    // Custom button class for tabs
    private class TabButtonWidget extends ButtonWidget {
        private final ItemStack stack;
        private final int slotIndex;

        public TabButtonWidget(int x, int y, int width, int height, int slotIndex, ItemStack stack) {
            super(x, y, width, height, Text.empty(), button -> {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client != null) {
                    // Save cursor position
                    StackBoxScreen.lastMouseX = client.mouse.getX();
                    StackBoxScreen.lastMouseY = client.mouse.getY();

                    // Set pending tab index
                    StackBoxScreen.pendingTabSlotIndex = slotIndex;

                    ClientPlayNetworking.send(new ModMessages.OpenTabPayload(slotIndex));
                }
            }, DEFAULT_NARRATION_SUPPLIER);
            this.stack = stack;
            this.slotIndex = slotIndex;
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            // Draw tab background
            context.fill(getX(), getY(), getX() + width, getY() + height, 0xFFC6C6C6); // Light gray

            // Draw borders manually
            int borderColor = 0xFF555555;
            context.fill(getX(), getY(), getX() + width, getY() + 1, borderColor); // Top
            context.fill(getX(), getY(), getX() + 1, getY() + height, borderColor); // Left
            context.fill(getX() + width - 1, getY(), getX() + width, getY() + height, borderColor); // Right
            context.fill(getX(), getY() + height - 1, getX() + width, getY() + height, borderColor); // Bottom

            // Draw item icon
            context.drawItem(stack, getX() + 5, getY() + 6);

            // Dim inactive tabs
            if (this.slotIndex != StackBoxScreen.this.currentTabSlotIndex) {
                // Draw a semi-transparent dark overlay on inactive tabs
                context.fill(getX(), getY(), getX() + width, getY() + height, 0x4D000000); // 30% transparent black
            }

            // Draw tooltip if hovered
            if (isHovered()) {
                context.drawItemTooltip(textRenderer, stack, mouseX, mouseY);
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        this.firstRender = true;

        // Detect which StackBox is currently open
        if (this.client != null && this.client.player != null && this.handler != null) {
            PlayerInventory inv = this.client.player.getInventory();
            this.currentTabSlotIndex = -1;

            // 1. Check pending tab switch
            if (pendingTabSlotIndex != -1) {
                if (pendingTabSlotIndex >= 0 && pendingTabSlotIndex < inv.size()) {
                    ItemStack stack = inv.getStack(pendingTabSlotIndex);
                    if (stack.getItem() instanceof StackBoxItem) {
                        this.currentTabSlotIndex = pendingTabSlotIndex;
                    }
                }
                pendingTabSlotIndex = -1; // Consume
            }

            // 2. If not found, check held items (initial open)
            if (this.currentTabSlotIndex == -1) {
                // Main hand
                ItemStack mainHand = this.client.player.getMainHandStack();
                if (mainHand.getItem() instanceof StackBoxItem) {
                    // Find the slot index (0-8)
                    for (int i = 0; i < 9; i++) {
                        if (inv.getStack(i) == mainHand) {
                            this.currentTabSlotIndex = i;
                            break;
                        }
                    }
                }
                // Off hand
                else if (this.client.player.getOffHandStack().getItem() instanceof StackBoxItem) {
                    this.currentTabSlotIndex = 40; // Offhand slot index
                }
            }
        }

        // Initialize tabs
        if (this.client != null && this.client.player != null) {
            PlayerInventory inv = this.client.player.getInventory();
            int tabX = this.x;
            int tabY = this.y - 28; // Draw above the GUI

            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.getItem() instanceof StackBoxItem) {
                    // Create a tab button for this StackBox
                    this.addDrawableChild(new TabButtonWidget(tabX, tabY, 26, 28, i, stack));
                    tabX += 28; // Move to next tab position
                }
            }
        }

        // Uniform button size for all buttons
        int buttonWidth = 52;
        int buttonHeight = 20;
        int buttonY = this.y + 50;

        // Button 1: Deposit All (left)
        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.stackbox.deposit_all"),
                button -> {
                    if (this.client != null && this.client.interactionManager != null) {
                        this.client.interactionManager.clickButton(this.handler.syncId,
                                StackBoxScreenHandler.BUTTON_DEPOSIT_ALL);
                    }
                })
                .dimensions(this.x + 8, buttonY, buttonWidth, buttonHeight)
                .build());

        // Button 2: Withdraw 64 (center)
        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.stackbox.withdraw_stack"),
                button -> {
                    if (this.client != null && this.client.interactionManager != null) {
                        this.client.interactionManager.clickButton(this.handler.syncId,
                                StackBoxScreenHandler.BUTTON_WITHDRAW_STACK);
                    }
                })
                .dimensions(this.x + 62, buttonY, buttonWidth, buttonHeight)
                .build());

        // Button 3: Fill Inventory (right)
        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.stackbox.fill_inventory"),
                button -> {
                    if (this.client != null && this.client.interactionManager != null) {
                        this.client.interactionManager.clickButton(this.handler.syncId,
                                StackBoxScreenHandler.BUTTON_FILL_INVENTORY);
                    }
                })
                .dimensions(this.x + 116, buttonY, buttonWidth, buttonHeight)
                .build());

        // Button 4: Auto-Collect Toggle (next to item slot, same size as others)
        int toggleButtonX = this.x + 110;
        int toggleButtonY = this.y + 20;
        this.autoCollectButton = this.addDrawableChild(ButtonWidget.builder(
                Text.literal(this.handler.getAutoCollectEnabled() ? "Auto: ON" : "Auto: OFF"),
                button -> {
                    if (this.client != null && this.client.interactionManager != null) {
                        this.client.interactionManager.clickButton(this.handler.syncId,
                                StackBoxScreenHandler.BUTTON_TOGGLE_AUTO_COLLECT);
                    }
                })
                .dimensions(toggleButtonX, toggleButtonY, buttonWidth, buttonHeight)
                .build());

        // Restore cursor position if saved - also do it here just in case render is
        // delayed
        if (lastMouseX != -1 && lastMouseY != -1 && this.client != null) {
            GLFW.glfwSetCursorPos(this.client.getWindow().getHandle(), lastMouseX, lastMouseY);
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        // Draw the GUI texture
        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                x,
                y,
                0f,
                0f,
                this.backgroundWidth,
                this.backgroundHeight,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Ensure cursor is restored on the very first render frame to prevent
        // flickering
        if (firstRender && lastMouseX != -1 && lastMouseY != -1 && this.client != null) {
            GLFW.glfwSetCursorPos(this.client.getWindow().getHandle(), lastMouseX, lastMouseY);

            // Recalculate mouseX/mouseY for this frame so tooltips appear at the correct
            // position immediately
            double scale = this.client.getWindow().getScaleFactor();
            mouseX = (int) (lastMouseX / scale);
            mouseY = (int) (lastMouseY / scale);

            firstRender = false;
            lastMouseX = -1;
            lastMouseY = -1;
        } else {
            firstRender = false;
        }

        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        // Draw Total Item count
        int count = this.handler.getStoredCount();
        String countText = formatCount(count);

        int textX = this.x + this.backgroundWidth / 2;
        int textY = this.y + 40; // Between slot (ends at 38) and buttons (start at 50)

        // Draw centered text without shadow (dark grey to match labels)
        int textWidth = this.textRenderer.getWidth(countText);
        context.drawText(this.textRenderer, countText, textX - textWidth / 2, textY, 0xFF404040, false);

        // Update auto-collect button text
        if (this.autoCollectButton != null) {
            this.autoCollectButton
                    .setMessage(Text.literal(this.handler.getAutoCollectEnabled() ? "Auto: ON" : "Auto: OFF"));
        }

        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    private String formatCount(int count) {
        if (count == 0) {
            return "Total Item: 0";
        }

        String itemId = this.handler.getStoredItemId();
        if (itemId.isEmpty()) {
            return "Total Item: " + String.format("%,d", count);
        }

        Identifier id = Identifier.tryParse(itemId);
        if (id == null || !Registries.ITEM.containsId(id)) {
            return "Total Item: " + String.format("%,d", count);
        }

        int maxStack = Registries.ITEM.get(id).getMaxCount();
        if (maxStack <= 1) {
            return "Total Item: " + String.format("%,d", count);
        }

        int stacks = count / maxStack;
        int remainder = count % maxStack;

        return String.format("%d stacks + %d (%s)", stacks, remainder, String.format("%,d", count));
    }
}
