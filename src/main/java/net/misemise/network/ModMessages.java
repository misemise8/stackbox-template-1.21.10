package net.misemise.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.misemise.item.StackBoxItem;
import net.misemise.screen.StackBoxScreenHandler;

public class ModMessages {
    public static final Identifier OPEN_TAB_PACKET_ID = Identifier.of("stackbox", "open_tab");

    // Define the payload
    public record OpenTabPayload(int slotIndex) implements CustomPayload {
        public static final CustomPayload.Id<OpenTabPayload> ID = new CustomPayload.Id<>(OPEN_TAB_PACKET_ID);
        public static final PacketCodec<RegistryByteBuf, OpenTabPayload> CODEC = PacketCodec.tuple(
                PacketCodecs.INTEGER, OpenTabPayload::slotIndex,
                OpenTabPayload::new);

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static void registerC2SPackets() {
        // Register the payload type
        PayloadTypeRegistry.playC2S().register(OpenTabPayload.ID, OpenTabPayload.CODEC);

        // Register the receiver
        ServerPlayNetworking.registerGlobalReceiver(OpenTabPayload.ID, (payload, context) -> {
            int slotIndex = payload.slotIndex();

            context.server().execute(() -> {
                // Verify the slot contains a StackBox
                PlayerInventory inventory = context.player().getInventory();
                if (slotIndex >= 0 && slotIndex < inventory.size()) {
                    ItemStack stack = inventory.getStack(slotIndex);
                    if (stack.getItem() instanceof StackBoxItem) {
                        // Open the new StackBox
                        NamedScreenHandlerFactory screenHandlerFactory = new SimpleNamedScreenHandlerFactory(
                                (syncId, playerInventory, playerEntity) -> new StackBoxScreenHandler(syncId,
                                        playerInventory, stack),
                                Text.translatable("item.stackbox.stack_box"));
                        context.player().openHandledScreen(screenHandlerFactory);
                    }
                }
            });
        });
    }

    public static void registerS2CPackets() {
        // No S2C packets needed for now
    }
}
