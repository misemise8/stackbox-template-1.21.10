package net.misemise.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.misemise.StackBox;

/**
 * Item registration for the Stack Box mod.
 * Handles registration of all custom items.
 */
public class ModItems {

    public static final Item STACK_BOX = register("stack_box",
            new StackBoxItem(new Item.Settings()
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(StackBox.MOD_ID, "stack_box")))
                    .maxCount(1)));

    /**
     * Register an item with the given name
     */
    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(StackBox.MOD_ID, name), item);
    }

    /**
     * Initialize and register all items
     */
    public static void initialize() {
        // Add to creative inventory
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(STACK_BOX);
        });

        StackBox.LOGGER.info("Registering items for " + StackBox.MOD_ID);
    }
}
