package net.misemise;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StackBox implements ModInitializer {
	public static final String MOD_ID = "stackbox";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Initialize items
		net.misemise.item.ModItems.initialize();

		// Initialize screen handlers
		net.misemise.screen.ModScreenHandlers.initialize();

		LOGGER.info("Stack Box mod initialized!");
	}
}