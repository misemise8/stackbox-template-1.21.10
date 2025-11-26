package net.misemise.screen;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.misemise.StackBox;

/**
 * Registration for custom screen handlers.
 */
public class ModScreenHandlers {

    public static final ScreenHandlerType<StackBoxScreenHandler> STACK_BOX_SCREEN_HANDLER = Registry.register(
            Registries.SCREEN_HANDLER,
            Identifier.of(StackBox.MOD_ID, "stack_box"),
            new ScreenHandlerType<>(StackBoxScreenHandler::new, null));

    /**
     * Initialize and register all screen handlers
     */
    public static void initialize() {
        StackBox.LOGGER.info("Registering screen handlers for " + StackBox.MOD_ID);
    }
}
