package com.eirmax.utils;

import com.eirmax.item.SwordOfWarden;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class SwordOfWardenHandler {
    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                SwordOfWarden.onPlayerTick(player);
            }
        });
    }
}
