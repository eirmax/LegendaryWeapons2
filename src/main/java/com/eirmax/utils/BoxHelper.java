package com.eirmax.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;

import java.util.List;

public class BoxHelper {
    public static void BoxHelperUtil(PlayerEntity viewer, double radius) {
        if (!(viewer.getWorld() instanceof ServerWorld world)) return;

        Box box = new Box(
                viewer.getX() - radius, viewer.getY() - radius, viewer.getZ() - radius,
                viewer.getX() + radius, viewer.getY() + radius, viewer.getZ() + radius
        );

        List<PlayerEntity> players = world.getEntitiesByClass(PlayerEntity.class, box, p -> p != viewer);

        for (PlayerEntity other : players) {
            GlowingUtil.setGlowingFor(viewer, other, true);
        }
    }
}
