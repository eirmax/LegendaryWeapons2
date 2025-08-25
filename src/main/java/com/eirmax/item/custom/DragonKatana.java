package com.eirmax.item.custom;

import com.eirmax.material.DragonKatanaTier;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.*;

public class DragonKatana extends SwordItem {

    private static final int COOLDOWN_TICKS = 150;
    private static final double MAX_TELEPORT_DISTANCE = 100.0;
    private static final int SPEED_EFFECT_DURATION = 20;
    private static final int SPEED_EFFECT_AMPLIFIER = 1;
    private static final double LAUNCH_CHANCE = 0.1;

    private static final int LEVITATION_DURATION = 6 * 20;
    private static final Random RANDOM = new Random();

    private static final Set<UUID> LEVITATING_PLAYERS = new HashSet<>();

    public DragonKatana(Settings settings) {
        super(new DragonKatanaTier(), 0, -2.2f, settings.fireproof());
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target instanceof PlayerEntity targetPlayer && attacker instanceof PlayerEntity attackerPlayer) {
            if (!attacker.getWorld().isClient) {
                boolean isCriticalHit = attackerPlayer.fallDistance > 0.0F
                        || attackerPlayer.getVelocity().y < 0
                        || attackerPlayer.isSprinting();

                if (isCriticalHit && RANDOM.nextDouble() < LAUNCH_CHANCE) {
                    launchPlayerIntoAir(targetPlayer, (ServerWorld) attacker.getWorld());
                }
            }
        }
        return super.postHit(stack, target, attacker);
    }

    private void launchPlayerIntoAir(PlayerEntity player, ServerWorld world) {
        LEVITATING_PLAYERS.add(player.getUuid());

        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.LEVITATION,
                LEVITATION_DURATION,
                4,
                false,
                true,
                true
        ));

        player.fallDistance = 0.0f;

        world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER,
                player.getX(), player.getY() - 1, player.getZ(),
                3, 0.0, 0.0, 0.0, 0.0);

        world.spawnParticles(ParticleTypes.EXPLOSION,
                player.getX(), player.getY() - 0.5, player.getZ(),
                20, 2.0, 0.5, 2.0, 0.1);

        world.spawnParticles(ParticleTypes.FLAME,
                player.getX(), player.getY() - 1, player.getZ(),
                30, 1.5, 0.3, 1.5, 0.2);

        world.spawnParticles(ParticleTypes.LARGE_SMOKE,
                player.getX(), player.getY() - 1, player.getZ(),
                25, 2.0, 0.5, 2.0, 0.1);

        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE,
                SoundCategory.PLAYERS, 2.5f, 0.8f);

        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_FLAP,
                SoundCategory.PLAYERS, 1.5f, 1.2f);
    }

    public static boolean isPlayerLevitating(PlayerEntity player) {
        return LEVITATING_PLAYERS.contains(player.getUuid()) &&
                player.hasStatusEffect(StatusEffects.LEVITATION);
    }

    public static void checkLevitatingPlayers(ServerWorld world) {
        LEVITATING_PLAYERS.removeIf(playerId -> {
            PlayerEntity player = world.getPlayerByUuid(playerId);

            if (player == null) {
                return true;
            }

            if (player.isOnGround() || !player.hasStatusEffect(StatusEffects.LEVITATION)) {
                if (player.isOnGround()) {
                    world.spawnParticles(ParticleTypes.CLOUD,
                            player.getX(), player.getY(), player.getZ(),
                            20, 1.0, 0.1, 1.0, 0.1);
                    world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP,
                            SoundCategory.PLAYERS, 1.0f, 1.2f);
                }
                return true;
            }

            long time = world.getTime();
            if (time % 10 == 0) {
                world.spawnParticles(ParticleTypes.DRAGON_BREATH,
                        player.getX(), player.getY() - 1, player.getZ(),
                        5, 0.3, 0.3, 0.3, 0.05);
            }

            player.fallDistance = 0.0f;

            return false;
        });
    }

    public static boolean shouldNegateFallDamage(PlayerEntity player) {
        return isHoldingDragonKatana(player) || isPlayerLevitating(player);
    }

    public static boolean isHoldingDragonKatana(PlayerEntity player) {
        ItemStack mainHand = player.getStackInHand(Hand.MAIN_HAND);
        ItemStack offHand = player.getStackInHand(Hand.OFF_HAND);
        return mainHand.getItem() instanceof DragonKatana || offHand.getItem() instanceof DragonKatana;
    }

    public static void applySpeedEffect(PlayerEntity player) {
        if (isHoldingDragonKatana(player)) {
            StatusEffectInstance currentSpeed = player.getStatusEffect(StatusEffects.SPEED);
            if (currentSpeed != null) {
                int newAmplifier = Math.min(currentSpeed.getAmplifier() + 1, 4);
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SPEED,
                        SPEED_EFFECT_DURATION,
                        newAmplifier,
                        false,
                        false,
                        false
                ));
            } else {
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SPEED,
                        SPEED_EFFECT_DURATION,
                        SPEED_EFFECT_AMPLIFIER,
                        false,
                        false,
                        false
                ));
            }
        }
    }


    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (player.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }

        if (!world.isClient) {
            Vec3d startPos = player.getEyePos();
            Vec3d lookDirection = player.getRotationVector();
            Vec3d endPos = startPos.add(lookDirection.multiply(MAX_TELEPORT_DISTANCE));

            BlockHitResult raycast = world.raycast(new RaycastContext(
                    startPos,
                    endPos,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    player
            ));

            Vec3d teleportPos;
            if (raycast.getType() != HitResult.Type.MISS) {
                BlockPos blockPos = raycast.getBlockPos();
                teleportPos = Vec3d.of(blockPos).add(0.5, 1.0, 0.5);

                if (!world.getBlockState(blockPos.up()).isAir() || !world.getBlockState(blockPos.up().up()).isAir()) {
                    teleportPos = teleportPos.add(0, 2, 0);
                }
            } else {
                teleportPos = endPos;
            }

            double distance = startPos.distanceTo(teleportPos);
            if (distance > MAX_TELEPORT_DISTANCE) {
                teleportPos = startPos.add(lookDirection.multiply(MAX_TELEPORT_DISTANCE));
            }

            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);

            player.teleport(teleportPos.x, teleportPos.y, teleportPos.z);

            world.playSound(null, teleportPos.x, teleportPos.y, teleportPos.z,
                    SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.2f);

            player.getItemCooldownManager().set(this, COOLDOWN_TICKS);
        }

        return TypedActionResult.success(stack);
    }

    @Override
    public void onCraft(ItemStack stack, World world, PlayerEntity player) {
        super.onCraft(stack, world, player);
        stack.addEnchantment(Enchantments.SHARPNESS, 3);
        stack.addEnchantment(Enchantments.UNBREAKING, 5);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.literal("Держа катану в руках вы чувствуете медленное дыхание Дракона Края").formatted(Formatting.LIGHT_PURPLE));
        tooltip.add(Text.literal("При использовании: телепортирует игрока в точку нахождения взгляда").formatted(Formatting.LIGHT_PURPLE));
        tooltip.add(Text.literal("Держа в главной руке игрок получает ускорение").formatted(Formatting.AQUA));
        tooltip.add(Text.literal("Удваивает эффект скорости если на игроке он уже есть").formatted(Formatting.AQUA));
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}