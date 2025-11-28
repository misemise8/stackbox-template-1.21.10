package net.misemise.screen;

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
import net.misemise.screen.StackBoxScreenHandler;

public class StackBoxScreen extends HandledScreen<StackBoxScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of("stackbox", "textures/gui/stack_box.png");
    private static final int TEXTURE_WIDTH = 176;
    private static final int TEXTURE_HEIGHT = 166;

    private ButtonWidget autoCollectButton;

    public StackBoxScreen(StackBoxScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 166;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    private final List<StackBoxTab> tabs = new ArrayList<>();

    private record StackBoxTab(int slotIndex, ItemStack stack, int x, int y, int width, int height) {
    }

    @Override
    protected void init() {
        super.init();

        // Initialize tabs
        this.tabs.clear();
        if (this.client != null && this.client.player != null) {
            PlayerInventory inv = this.client.player.getInventory();
            int tabX = this.x;
            int tabY = this.y - 28; // Draw above the GUI

            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.getItem() instanceof net.misemise.item.StackBoxItem) {
                    // Create a tab for this StackBox
                    this.tabs.add(new StackBoxTab(i, stack, tabX, tabY, 26, 28));
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
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        // Draw tabs first so they appear behind the main background if needed,
        // or after if we want them on top. Creative tabs are usually behind.
        renderTabs(context, mouseX, mouseY);

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

    private void renderTabs(DrawContext context, int mouseX, int mouseY) {
        for (StackBoxTab tab : this.tabs) {
            // Draw tab background
            // Using a simple colored rectangle for now, or part of the texture if we had
            // one.
            // Let's use a darkened rectangle for inactive tabs and lighter for active?
            // Since we don't easily know which is active, we'll draw them all same for now.

            // Draw tab background (simulated with color)
            context.fill(tab.x, tab.y, tab.x + tab.width, tab.y + tab.height, 0xFFC6C6C6); // Light gray

            // Draw borders manually
            int borderColor = 0xFF555555;
            context.fill(tab.x, tab.y, tab.x + tab.width, tab.y + 1, borderColor); // Top
            context.fill(tab.x, tab.y, tab.x + 1, tab.y + tab.height, borderColor); // Left
            context.fill(tab.x + tab.width - 1, tab.y, tab.x + tab.width, tab.y + tab.height, borderColor); // Right
            context.fill(tab.x, tab.y + tab.height - 1, tab.x + tab.width, tab.y + tab.height, borderColor); // Bottom

            // Draw item icon
            context.drawItem(tab.stack, tab.x + 5, tab.y + 6);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left click
            for (StackBoxTab tab : this.tabs) {
                if (mouseX >= tab.x && mouseX < tab.x + tab.width &&
                        mouseY >= tab.y && mouseY < tab.y + tab.height) {

                    // Send packet to open this tab
                    if (this.client != null) {
                        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                                new net.misemise.network.ModMessages.OpenTabPayload(tab.slotIndex));
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        // Draw tooltips for tabs
        for (StackBoxTab tab : this.tabs) {
            if (mouseX >= tab.x && mouseX < tab.x + tab.width &&
                    mouseY >= tab.y && mouseY < tab.y + tab.height) {
                context.drawItemTooltip(this.textRenderer, tab.stack, mouseX, mouseY);
            }
        }

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
