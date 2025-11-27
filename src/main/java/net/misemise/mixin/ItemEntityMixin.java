package net.misemise.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;
import net.misemise.item.StackBoxItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    @Shadow
    public abstract ItemStack getStack();

    @Shadow
    private int pickupDelay;

    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "onPlayerCollision", at = @At("HEAD"))
    private void onPlayerCollision(PlayerEntity player, CallbackInfo ci) {
        if (this.getEntityWorld().isClient())
            return;
        if (this.pickupDelay > 0)
            return;

        ItemStack pickedStack = this.getStack();
        if (pickedStack.isEmpty())
            return;

        // Iterate through player's inventory to find StackBoxes
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack inventoryStack = player.getInventory().getStack(i);

            if (inventoryStack.getItem() instanceof StackBoxItem) {
                if (!StackBoxItem.isAutoCollectEnabled(inventoryStack)) {
                    continue;
                }
                String storedId = StackBoxItem.getStoredItemId(inventoryStack);
                String pickedId = Registries.ITEM.getId(pickedStack.getItem()).toString();

                if (!storedId.isEmpty() && storedId.equals(pickedId)) {
                    int countToAdd = pickedStack.getCount();
                    int overflow = StackBoxItem.addItems(inventoryStack, storedId, countToAdd);

                    if (overflow != countToAdd) {
                        // Items were added
                        pickedStack.setCount(overflow);

                        // Play pickup sound
                        player.getEntityWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                                net.minecraft.sound.SoundEvents.ENTITY_ITEM_PICKUP,
                                net.minecraft.sound.SoundCategory.PLAYERS,
                                0.2F,
                                ((player.getEntityWorld().random.nextFloat()
                                        - player.getEntityWorld().random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                    }

                    if (pickedStack.isEmpty()) {
                        break;
                    }
                }
            }
        }
    }
}
