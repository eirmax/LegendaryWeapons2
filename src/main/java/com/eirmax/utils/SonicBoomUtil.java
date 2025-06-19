package com.eirmax.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class SonicBoomUtil {
    public static void shootSonicBoom(PlayerEntity owner, Entity target) {
        owner.getWorld().playSound(null, owner.getBlockPos(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, owner.getSoundCategory(), 2.0f, 1.0f);
        if (owner.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM, target.getX(), target.getBodyY(0.5), target.getZ(), 1, 0,0,0,0);
        }
        SonicBoomUtil.wardExplosion(owner, target);
    }
    public static void wardExplosion(PlayerEntity owner, Entity target) {
        float damage = 8 + 3;
        target.damage(owner.getDamageSources().sonicBoom(owner), damage);
        if (target instanceof LivingEntity living) {
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 20*20, 0));
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20*20, 2));
        }
        target.getWorld().createExplosion(owner, target.getX(), target.getY(), target.getZ(), 2.0f, World.ExplosionSourceType.NONE);
    }
}