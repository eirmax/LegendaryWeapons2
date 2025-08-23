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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class DragonKatana extends SwordItem {

    private static final int COOLDOWN_TICKS = 150;
    private static final double MAX_TELEPORT_DISTANCE = 300.0;
    private static final int SPEED_EFFECT_DURATION = 20;
    private static final int SPEED_EFFECT_AMPLIFIER = 1;
    private static final double LAUNCH_CHANCE = 0.1;
    private static final int LAUNCH_HEIGHT = 100;
    private static final int LEVITATION_DURATION = 20 * 20;
    private static final Random RANDOM = new Random();

    private static final Map<UUID, Long> LAUNCHED_PLAYERS = new HashMap<>();

    public DragonKatana(Settings settings) {
        super(new DragonKatanaTier(), 0, -2.4f, settings.fireproof());
    }


    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target instanceof PlayerEntity targetPlayer && attacker instanceof PlayerEntity attackerPlayer) {
            if (!attacker.getWorld().isClient) {
                boolean isCriticalHit = attackerPlayer.fallDistance > 0.0F ||
                        attackerPlayer.getVelocity().y < 0 ||
                        attackerPlayer.isSprinting();

                if (isCriticalHit && RANDOM.nextDouble() < LAUNCH_CHANCE) {
                    launchPlayerIntoAir(targetPlayer, (ServerWorld) attacker.getWorld());
                }
            }
        }

        return super.postHit(stack, target, attacker);
    }


    private void launchPlayerIntoAir(PlayerEntity player, ServerWorld world) {
        LAUNCHED_PLAYERS.put(player.getUuid(), world.getTime() + LEVITATION_DURATION);

        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.LEVITATION,
                100,
                4,
                false,
                true,
                true
        ));

        player.fallDistance = 0.0f;

        world.spawnParticles(ParticleTypes.DRAGON_BREATH,
                player.getX(), player.getY(), player.getZ(),
                50, 1.0, 2.0, 1.0, 0.3);

        world.spawnParticles(ParticleTypes.END_ROD,
                player.getX(), player.getY(), player.getZ(),
                30, 0.5, 1.0, 0.5, 0.2);

        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_FLAP,
                SoundCategory.PLAYERS, 2.0f, 1.5f);
    }


    public static boolean isPlayerLaunched(PlayerEntity player) {
        return LAUNCHED_PLAYERS.containsKey(player.getUuid());
    }


    public static void handleLaunchedPlayers(ServerWorld world) {
        long currentTime = world.getTime();

        LAUNCHED_PLAYERS.entrySet().removeIf(entry -> {
            UUID playerId = entry.getKey();
            long endTime = entry.getValue();

            PlayerEntity player = world.getPlayerByUuid(playerId);
            if (player == null) {
                return true;
            }

            if (player.isOnGround()) {
                world.spawnParticles(ParticleTypes.CLOUD,
                        player.getX(), player.getY(), player.getZ(),
                        20, 1.0, 0.1, 1.0, 0.1);

                world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP,
                        SoundCategory.PLAYERS, 1.0f, 1.2f);

                return true;
            }

            if (currentTime > endTime) {
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SLOW_FALLING,
                        200,
                        0,
                        false,
                        true,
                        true
                ));
                return true;
            }

            player.fallDistance = 0.0f;

            if (currentTime % 10 == 0) {
                world.spawnParticles(ParticleTypes.DRAGON_BREATH,
                        player.getX(), player.getY() - 1, player.getZ(),
                        5, 0.3, 0.3, 0.3, 0.05);
            }

            return false;
        });
    }


    public static boolean shouldNegateFallDamage(PlayerEntity player) {
        return isHoldingDragonKatana(player) || isPlayerLaunched(player);
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

        tooltip.add(Text.literal("Держа катану в руках вы чувствуете медленное дыхание Дракона Края").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("При использовании: телепортирует игрока в точку нахождения взгляда").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("Держа в главной руке игрок получает ускорение").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("Удвает эффект скорости если на игроке он уже есть").formatted(Formatting.GRAY));
    }


    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}