package net.misemise.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.registry.Registries;

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

    @Override
    protected void init() {
        super.init();

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

        // Button 4: Auto-Collect Toggle (next to item slot)
        int toggleButtonWidth = 35;
        int toggleButtonHeight = 16;
        int toggleButtonX = this.x + 110; // Next to the item slot
        int toggleButtonY = this.y + 20; // Same height as item slot
        this.autoCollectButton = this.addDrawableChild(ButtonWidget.builder(
                Text.literal(this.handler.getAutoCollectEnabled() ? "Auto: ON" : "Auto: OFF"),
                button -> {
                    if (this.client != null && this.client.interactionManager != null) {
                        this.client.interactionManager.clickButton(this.handler.syncId,
                                StackBoxScreenHandler.BUTTON_TOGGLE_AUTO_COLLECT);
                    }
                })
                .dimensions(toggleButtonX, toggleButtonY, toggleButtonWidth, toggleButtonHeight)
                .build());
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
