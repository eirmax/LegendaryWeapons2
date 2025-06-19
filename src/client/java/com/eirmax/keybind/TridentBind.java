package com.eirmax.keybind;

import com.eirmax.item.ModItems;
import com.eirmax.packets.PoseidonStrikePacketClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.item.ItemStack;

public class TridentBind implements ClientModInitializer {
        @Override
        public void onInitializeClient() {
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                while (TridentKeyBind.trident_key.wasPressed()) {
                    if (client.player != null) {
                        ItemStack held = client.player.getMainHandStack();
                        if (held.getItem() == ModItems.POSEIDONSTRIDENT) {
                            PoseidonStrikePacketClient.send();
                        }
                    }
                }
            });
        }
}
