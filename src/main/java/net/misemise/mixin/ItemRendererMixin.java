package net.misemise.mixin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.misemise.item.StackBoxItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Shadow
    public abstract void renderItem(ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded,
            MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model);

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V", at = @At("TAIL"))
    private void renderStackBoxOverlay(ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded,
            MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model,
            CallbackInfo ci) {
        if (stack.getItem() instanceof StackBoxItem) {
            String storedItemId = StackBoxItem.getStoredItemId(stack);
            if (!storedItemId.isEmpty()) {
                Identifier id = Identifier.tryParse(storedItemId);
                if (id != null && Registries.ITEM.containsId(id)) {
                    ItemStack storedStack = new ItemStack(Registries.ITEM.get(id));

                    matrices.push();

                    // Adjust position and scale for the overlay
                    // Move to center and slightly forward to avoid z-fighting
                    if (renderMode == ModelTransformationMode.GUI) {
                        matrices.translate(8.0f, 8.0f, 1.0f); // Center in GUI slot (16x16)
                        matrices.scale(0.5f, 0.5f, 0.5f); // Half size
                        matrices.translate(-8.0f, -8.0f, 0.0f); // Center scaling
                    } else {
                        // For 3D rendering (hand, ground), we might need different adjustments
                        // But for now let's try a generic small overlay
                        matrices.translate(0.5f, 0.5f, 0.55f); // Center and slightly forward
                        matrices.scale(0.5f, 0.5f, 0.5f);
                        matrices.translate(-0.5f, -0.5f, 0.0f);
                    }

                    // Render the stored item
                    // We pass the same renderMode to keep lighting/shading consistent
                    this.renderItem(storedStack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay,
                            null);

                    matrices.pop();
                }
            }
        }
    }
}
