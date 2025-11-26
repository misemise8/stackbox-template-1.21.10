package net.misemise.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Client-side GUI screen for the Stack Box.
 * Displays the storage slot, player inventory, and three action buttons.
 */
public class StackBoxScreen extends HandledScreen<StackBoxScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of("stackbox", "textures/gui/stack_box.png");

    public StackBoxScreen(StackBoxScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 133;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 50;
        int buttonHeight = 20;
        int buttonY = this.y + 45;

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

        // Button 2: Withdraw Stack (center)
        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.stackbox.withdraw_stack"),
                button -> {
                    if (this.client != null && this.client.interactionManager != null) {
                        this.client.interactionManager.clickButton(this.handler.syncId,
                                StackBoxScreenHandler.BUTTON_WITHDRAW_STACK);
                    }
                })
                .dimensions(this.x + 63, buttonY, buttonWidth, buttonHeight)
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
                .dimensions(this.x + 118, buttonY, buttonWidth, buttonHeight)
                .build());
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        // Draw a simple background using fillGradient as workaround
        // TODO: Find correct way to render GUI texture in Minecraft 1.21.10
        context.fillGradient(x, y, x + this.backgroundWidth, y + this.backgroundHeight, 0xC0101010, 0xD0101010);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);

        // Draw stored item count
        int count = this.handler.getStoredCount();
        String itemId = this.handler.getStoredItemId();

        if (!itemId.isEmpty() && count > 0) {
            String countText = formatCount(count);
            int textX = this.x + this.backgroundWidth / 2;
            int textY = this.y + 40;

            // Draw centered text
            int textWidth = this.textRenderer.getWidth(countText);
            context.drawText(this.textRenderer, countText, textX - textWidth / 2, textY, 0x404040, false);
        }
    }

    private String formatCount(int count) {
        if (count >= 1_000_000) {
            return String.format("%,d", count);
        } else if (count >= 1_000) {
            return String.format("%,d", count);
        } else {
            return String.valueOf(count);
        }
    }
}
