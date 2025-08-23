package com.eirmax.event;

import com.eirmax.item.custom.SwordOfMidas;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class SwordOfMidasEventHandler {

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(SwordOfMidasEventHandler::onServerTick);
    }

    public static void onServerTick(MinecraftServer server) {
        if (server.getTicks() % 20 == 0) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                SwordOfMidas.applyAbsorptionEffect(player);
            }
            for (ServerWorld world : server.getWorlds()) {
                SwordOfMidas.handleBurningPlayers(world);
            }
        }
    }
}