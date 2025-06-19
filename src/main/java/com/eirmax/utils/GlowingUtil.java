package com.eirmax.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;

public class GlowingUtil {
    public static void setGlowingFor(PlayerEntity viewer, Entity target, boolean glowing) {
        target.setGlowing(glowing);

        if (viewer instanceof ServerPlayerEntity serverPlayer) {
            EntityTrackerUpdateS2CPacket packet = new EntityTrackerUpdateS2CPacket(target.getId(), target.getDataTracker().getChangedEntries());
            serverPlayer.networkHandler.sendPacket(packet);
        }
    }
}