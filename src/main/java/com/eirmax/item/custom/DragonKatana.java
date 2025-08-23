package com.eirmax.item.custom;

import com.eirmax.material.DragonKatanaTier;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
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

import java.util.List;

public class DragonKatana extends SwordItem {

    private static final int COOLDOWN_TICKS = 150;
    private static final double MAX_TELEPORT_DISTANCE = 300.0;
    private static final int SPEED_EFFECT_DURATION = 20;
    private static final int SPEED_EFFECT_AMPLIFIER = 1;

    public DragonKatana(Settings settings) {
        super(new DragonKatanaTier(), 0, -2.4f, settings.fireproof());
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


    public static boolean shouldNegateFallDamage(PlayerEntity player) {
        return isHoldingDragonKatana(player);
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

        tooltip.add(Text.literal("Телепортация на ПКМ (100 блоков максимум)").formatted(Formatting.DARK_PURPLE));
        tooltip.add(Text.literal("Кулдаун: 15 секунд").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("Отключает урон от падения при удержании").formatted(Formatting.GREEN));
        tooltip.add(Text.literal("Дает скорость 2 при удержании").formatted(Formatting.AQUA));
        tooltip.add(Text.literal("Эффект удваивается при повторном применении").formatted(Formatting.AQUA));
        tooltip.add(Text.literal("Легендарная катана Эндера").formatted(Formatting.LIGHT_PURPLE));
    }


    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}