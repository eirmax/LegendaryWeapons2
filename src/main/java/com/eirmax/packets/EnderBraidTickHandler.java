package com.eirmax.packets;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;


public class EnderBraidTickHandler {
    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(EnderBraidLastTickHandler::onServerTick);
    }
}
