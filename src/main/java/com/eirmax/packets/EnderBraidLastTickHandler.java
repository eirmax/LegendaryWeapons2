package com.eirmax.packets;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public class EnderBraidLastTickHandler {

    private static final HashMap<UUID, Integer> sentencedPlayers = new HashMap<>();
    private static final int DAMAGE_INTERVAL = 200;

    public static void applySentence(ServerPlayerEntity player) {
        if (!sentencedPlayers.containsKey(player.getUuid())) {
            sentencedPlayers.put(player.getUuid(), 0);
        }
    }

    public static void onServerTick(ServerWorld world) {
        Iterator<UUID> it = sentencedPlayers.keySet().iterator();
        while (it.hasNext()) {
            UUID uuid = it.next();
            PlayerEntity player = world.getPlayerByUuid(uuid);
            if (player == null) {
                continue;
            }
            if (player.getHealth() > player.getMaxHealth() - 0.5f) {
                player.setHealth(player.getMaxHealth() - 0.5f);
            }
            int ticks = sentencedPlayers.get(uuid) + 1;
            if (ticks >= DAMAGE_INTERVAL) {
                if (player.getHealth() > 1.0f) {
                    player.damage(world.getDamageSources().magic(), 1.0f);
                }
                ticks = 0;
            }
            if (player.getHealth() <= 1.0f) {
                it.remove();
                continue;
            }
            sentencedPlayers.put(uuid, ticks);
        }
    }

    public static boolean isSentenced(PlayerEntity player) {
        return sentencedPlayers.containsKey(player.getUuid());
    }
}
