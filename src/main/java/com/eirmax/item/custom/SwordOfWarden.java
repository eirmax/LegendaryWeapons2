package com.eirmax.item.custom;

import com.eirmax.material.SwordOfWardenTier;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;


public class SwordOfWarden extends SwordItem{


    //CONSTANTS

    private static final int COOLDOWN_TICKS = 450;
    public static final int ATTACK_DAMAGE = 9;
    public static final float ATTACK_SPEED = -2.7f;
    public static final int ENCHANTABILITY = 15;
    public static final int SONIC_RANGE = 100;
    public static final int GLOW_RADIUS = 200;
    public static final int EFFECT_DURATION = 20 * 20;
    public static final int SLOWNESS_LEVEL = 2;
    public static final float BOOM_DAMAGE = 11.0f;
    public static final float BOOM_EXPLOSION = 2.0f;

    public SwordOfWarden(Settings settings) {
        super(new SwordOfWardenTier(), 0, ATTACK_SPEED, new Item.Settings().fireproof().maxCount(1));
    }


    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof PlayerEntity) {
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, EFFECT_DURATION, 0));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, EFFECT_DURATION, SLOWNESS_LEVEL));
        }
        return super.postHit(stack, target, attacker);
    }



    public static void onPlayerTick(ServerPlayerEntity player) {
        boolean hasSword = hasShadowSword(player);

        if (hasSword && player.isSneaking()) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 10, 0, false, false, false));

            highlightPlayersFor(player, GLOW_RADIUS);

        } else {
            if (player.hasStatusEffect(StatusEffects.INVISIBILITY)) {
                player.removeStatusEffect(StatusEffects.INVISIBILITY);
            }
            removeHighlightFor(player, GLOW_RADIUS);
        }
    }


    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            shootSonicBoomLine((ServerWorld) world, user);
        }
        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(user.getStackInHand(hand));
        }
        user.getItemCooldownManager().set(this, COOLDOWN_TICKS);
        return TypedActionResult.success(user.getStackInHand(hand), world.isClient());
    }


    public void shootSonicBoomLine(ServerWorld world, PlayerEntity user) {
        Vec3d start = user.getEyePos();
        Vec3d look = user.getRotationVec(1.0F);
        double step = 0.5;
        double maxDist = SONIC_RANGE;
        boolean hasHit = false;

        for (double d = 0; d < maxDist; d += step) {
            Vec3d pos = start.add(look.multiply(d));
            world.spawnParticles(ParticleTypes.SONIC_BOOM, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0.0);
            Box box = new Box(pos.subtract(0.5, 0.5, 0.5), pos.add(0.5, 0.5, 0.5));
            List<LivingEntity> hit = world.getEntitiesByClass(LivingEntity.class, box,
                    e -> e != user && e.isAlive() && !(e instanceof PlayerEntity p && p.isSpectator()));

            if (!hit.isEmpty()) {
                LivingEntity target = hit.get(0);

                target.damage(world.getDamageSources().sonicBoom(user), BOOM_DAMAGE);

                target.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, EFFECT_DURATION, 0));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, EFFECT_DURATION, SLOWNESS_LEVEL));


                world.createExplosion(user, target.getX(), target.getY(), target.getZ(), BOOM_EXPLOSION, World.ExplosionSourceType.NONE);

                Box explosionBox = new Box(
                        target.getX() - BOOM_EXPLOSION, target.getY() - BOOM_EXPLOSION, target.getZ() - BOOM_EXPLOSION,
                        target.getX() + BOOM_EXPLOSION, target.getY() + BOOM_EXPLOSION, target.getZ() + BOOM_EXPLOSION
                );

                List<LivingEntity> explosionTargets = world.getEntitiesByClass(LivingEntity.class, explosionBox,
                        e -> e != user && e.isAlive() && !(e instanceof PlayerEntity p && p.isSpectator()));

                for (LivingEntity explosionTarget : explosionTargets) {
                    explosionTarget.damage(world.getDamageSources().explosion(user, user), 10.0f);

                    explosionTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, EFFECT_DURATION / 2, 0));
                    explosionTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, EFFECT_DURATION / 2, SLOWNESS_LEVEL - 1));
                }

                hasHit = true;
                break;
            }
        }

        world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_WARDEN_SONIC_BOOM, user.getSoundCategory(), 2.0f, 1.0f);
    }


    public static boolean hasShadowSword(PlayerEntity player) {
        for (ItemStack stack : player.getInventory().main) {
            if (stack.getItem() instanceof SwordOfWarden) return true;
        }
        return false;
    }


    public static void highlightPlayersFor(PlayerEntity viewer, double radius) {
        if (!(viewer.getWorld() instanceof net.minecraft.server.world.ServerWorld world)) return;

        Box box = new Box(
                viewer.getX() - radius, viewer.getY() - radius, viewer.getZ() - radius,
                viewer.getX() + radius, viewer.getY() + radius, viewer.getZ() + radius
        );

        List<PlayerEntity> players = world.getEntitiesByClass(PlayerEntity.class, box, p -> p != viewer);

        for (PlayerEntity other : players) {
            setGlowingForViewerOnly(viewer, other, true);
        }
    }

    public static void setGlowingForViewerOnly(PlayerEntity viewer, LivingEntity target, boolean glowing) {
        if (viewer instanceof ServerPlayerEntity serverPlayer) {

            serverPlayer.networkHandler.sendPacket(
                    new EntityTrackerUpdateS2CPacket(target.getId(), target.getDataTracker().getChangedEntries())
            );

            boolean wasGlowing = target.isGlowing();
            target.setGlowing(glowing);

            serverPlayer.networkHandler.sendPacket(
                    new EntityTrackerUpdateS2CPacket(target.getId(), target.getDataTracker().getChangedEntries())
            );

            target.setGlowing(wasGlowing);
        }
    }


    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        tooltip.add(Text.literal("В прошлом простой алмазный меч пронзивший Вардена много веков назад...").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("При использовании: выстреливает потоком звука в противника").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("При ударе по противнику оглушает его").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("При зажатой клавиши SHIFT подсвечивает игроков поблизости и ").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("делает невидимым владельца меча").formatted(Formatting.GRAY));
    }


    public static void removeHighlightFor(PlayerEntity viewer, double radius) {
        if (!(viewer.getWorld() instanceof ServerWorld world)) return;

        Box box = new Box(
                viewer.getX() - radius, viewer.getY() - radius, viewer.getZ() - radius,
                viewer.getX() + radius, viewer.getY() + radius, viewer.getZ() + radius
        );

        List<PlayerEntity> players = world.getEntitiesByClass(PlayerEntity.class, box, p -> p != viewer);

        for (PlayerEntity other : players) {
            setGlowingForViewerOnly(viewer, other, false);
        }
    }


    @Override
    public int getEnchantability() {
        return ENCHANTABILITY;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }


    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return false;
    }
}

