package com.eirmax.utils;

import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class VanishUtil {
    public static void vanish(ServerPlayerEntity self, boolean vanish) {
        ServerWorld world = self.getServerWorld();
        for (ServerPlayerEntity other : world.getPlayers()) {
            if (other != self) {
                if (vanish) {
                    other.networkHandler.sendPacket(
                            new EntitiesDestroyS2CPacket(self.getId())
                    );
                } else {
                    other.networkHandler.sendPacket(
                            new PlayerSpawnS2CPacket(self)
                    );
                }
            }
        }
    }
}