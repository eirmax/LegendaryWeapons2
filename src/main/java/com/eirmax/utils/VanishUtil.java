package com.eirmax.utils;

import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.player.PlayerEntity;

public class VanishUtil {
    public static void vanish(ServerPlayerEntity self, boolean vanish) {
        ServerWorld world = self.getServerWorld();
        for (PlayerEntity other : world.getPlayers()) {
            if (other != self) {
                if (vanish) {
                    ((ServerPlayerEntity)other).networkHandler.sendPacket(
                            new EntitiesDestroyS2CPacket(self.getId())
                    );
                } else {
                    ((ServerPlayerEntity)other).networkHandler.sendPacket(
                            new PlayerSpawnS2CPacket(self)
                    );
                }
            }
        }
    }
}