package net.misemise;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StackBox implements ModInitializer {
	public static final String MOD_ID = "stackbox";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		// Initialize items
		net.misemise.item.ModItems.initialize();

		// Initialize screen handlers
		net.misemise.screen.ModScreenHandlers.initialize();

		LOGGER.info("Stack Box mod initialized!");
	}
}