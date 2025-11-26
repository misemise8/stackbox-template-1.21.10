package net.misemise.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.gl.RenderPipelines;

/**
 * Client-side GUI screen for the Stack Box.
 * Displays the storage slot, player inventory, and three action buttons.
 */
public class StackBoxScreen extends HandledScreen<StackBoxScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of("stackbox", "textures/gui/stack_box.png");
    private static final int TEXTURE_WIDTH = 176;
    private static final int TEXTURE_HEIGHT = 133;

    public StackBoxScreen(StackBoxScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = TEXTURE_HEIGHT;
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

        // Try to draw texture, fallback to colored background if it fails
        try {
            // Method 1: Standard texture drawing for 1.21.10
            context.drawTexture(
                    RenderPipelines.GUI_TEXTURED, // RenderPipeline（環境で名前が異なる場合あり、補完で確認）
                    TEXTURE,
                    x,
                    y,
                    0f, // u を float に
                    0f, // v を float に
                    this.backgroundWidth, // regionWidth
                    this.backgroundHeight, // regionHeight
                    TEXTURE_WIDTH, // textureWidth（画像の実サイズ）
                    TEXTURE_HEIGHT // textureHeight（画像の実サイズ）
            );
        } catch (Exception e) {
            // Fallback: Draw a simple colored background
            drawFallbackBackground(context, x, y);
        }
    }

    /**
     * Fallback rendering method if texture fails to load
     */
    private void drawFallbackBackground(DrawContext context, int x, int y) {
        // Draw main background
        context.fill(x, y, x + this.backgroundWidth, y + this.backgroundHeight, 0xFF8B8B8B);

        // Draw border
        context.fill(x, y, x + this.backgroundWidth, y + 1, 0xFF373737); // Top
        context.fill(x, y, x + 1, y + this.backgroundHeight, 0xFF373737); // Left
        context.fill(x + this.backgroundWidth - 1, y, x + this.backgroundWidth, y + this.backgroundHeight, 0xFFFFFFFF); // Right
        context.fill(x, y + this.backgroundHeight - 1, x + this.backgroundWidth, y + this.backgroundHeight, 0xFFFFFFFF); // Bottom

        // Draw slot background
        int slotX = x + 79;
        int slotY = y + 19;
        context.fill(slotX, slotY, slotX + 18, slotY + 18, 0xFF373737);

        // Draw inventory area
        context.fill(x + 7, y + 50, x + 169, y + 126, 0xFF8B8B8B);
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
            int textY = this.y + 30;

            // Draw centered text with shadow
            int textWidth = this.textRenderer.getWidth(countText);
            context.drawText(this.textRenderer, countText, textX - textWidth / 2, textY, 0xFFFFFF, true);
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