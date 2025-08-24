package com.eirmax.event;

import com.eirmax.item.custom.DragonKatana;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class DragonKatanaEventHandler {

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(DragonKatanaEventHandler::onServerTick);
        ServerTickEvents.START_WORLD_TICK.register(DragonKatana::handleLaunchedPlayers);
    }

    public static void onServerTick(MinecraftServer server) {
        if (server.getTicks() % 20 == 0) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                DragonKatana.applySpeedEffect(player);
            }
        }

        for (ServerWorld world : server.getWorlds()) {
            DragonKatana.handleLaunchedPlayers(world);
        }
    }
}