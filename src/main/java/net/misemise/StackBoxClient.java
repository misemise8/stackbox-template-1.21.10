package net.misemise;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.misemise.screen.ModScreenHandlers;
import net.misemise.screen.StackBoxScreen;

/**
 * Client-side initialization for the Stack Box mod.
 */
public class StackBoxClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register the screen
        HandledScreens.register(ModScreenHandlers.STACK_BOX_SCREEN_HANDLER, StackBoxScreen::new);

        StackBox.LOGGER.info("Client initialization complete for " + StackBox.MOD_ID);
    }
}
