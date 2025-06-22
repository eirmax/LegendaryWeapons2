package com.eirmax.item.cutom;

import com.eirmax.packets.EnderBraidLastTickHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class EnderBraid extends Item {

    //CONSTANTS







    private static final String SENTENCE_USED_KEY = "SentenceUsed";
    private static final int TELEPORT_RANGE = 70;

    public EnderBraid(Settings settings) {
        super(settings);
    }


    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof PlayerEntity player && target instanceof PlayerEntity byattacked) {
            if (player.fallDistance > 0.0F && !player.isOnGround() && !player.isClimbing() && !player.isTouchingWater() && !player.hasStatusEffect(StatusEffects.BLINDNESS) && !player.hasVehicle()) {
                if (player.getRandom().nextFloat() < 0.1f) {
                    byattacked.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 60, 4));
                }
            }
        }
        return super.postHit(stack, target, attacker);
    }


    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            Vec3d eyePos = user.getCameraPosVec(1.0f);
            Vec3d look = user.getRotationVec(1.0f);
            Vec3d reach = eyePos.add(look.multiply(TELEPORT_RANGE));
            Box box = user.getBoundingBox().stretch(look.multiply(TELEPORT_RANGE)).expand(1.0D, 1.0D, 1.0D);
            BlockHitResult hit = world.raycast(new RaycastContext(
                    eyePos, reach, net.minecraft.world.RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE, user
            ));
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockPos tp = hit.getBlockPos().offset(hit.getSide()).up(1); // +1 Y
                if (world.isAir(tp) && world.isAir(tp.up())) {
                    user.requestTeleport(tp.getX() + 0.5, tp.getY(), tp.getZ() + 0.5);
                    world.playSound(null, tp, SoundEvents.ENTITY_ENDERMAN_TELEPORT, user.getSoundCategory(), 1.0f, 1.0f);
                }
            }
        }
        return TypedActionResult.success(user.getStackInHand(hand), world.isClient());
    }


    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!user.getWorld().isClient && entity instanceof PlayerEntity target) {
            if (user.isSneaking() && !stack.getOrCreateNbt().getBoolean(SENTENCE_USED_KEY)) {
                stack.getOrCreateNbt().putBoolean(SENTENCE_USED_KEY, true);
                applySentence((ServerPlayerEntity)target);
                return ActionResult.CONSUME;
            }
        }
        return ActionResult.PASS;
    }


    private void applySentence(ServerPlayerEntity player) {
        EnderBraidLastTickHandler.applySentence(player);
    }
}