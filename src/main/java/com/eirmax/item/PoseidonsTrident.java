package com.eirmax.item;

import com.eirmax.components.PlayerEntityMixinAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;


public class PoseidonsTrident extends TridentItem {

    public static final int STRIKE_RADIUS = 10;
    public static final int STRIKE_COOLDOWN_TICKS = 2400; // 40 секунд

    public PoseidonsTrident(Settings settings) {
        super(settings);
    }

    public static void strikeLightningAOE(PlayerEntity player) {


        if (player.getWorld().isClient) return;

        ItemStack held = player.getMainHandStack();
        if (!(held.getItem() instanceof PoseidonsTrident)) return;

        if (player.getItemCooldownManager().isCoolingDown(held.getItem())) return;
        player.getItemCooldownManager().set(held.getItem(), STRIKE_COOLDOWN_TICKS);

        var world = (ServerWorld) player.getWorld();
        var box = new Box(
                player.getX() - STRIKE_RADIUS, player.getY() - STRIKE_RADIUS, player.getZ() - STRIKE_RADIUS,
                player.getX() + STRIKE_RADIUS, player.getY() + STRIKE_RADIUS, player.getZ() + STRIKE_RADIUS
        );

        for (var entity : world.getOtherEntities(player, box, e -> e instanceof LivingEntity && e.isAlive())) {
            var bolt = EntityType.LIGHTNING_BOLT.create(world);
            if (bolt != null) {
                bolt.refreshPositionAfterTeleport(entity.getX(), entity.getY(), entity.getZ());
                world.spawnEntity(bolt);
            }
        }
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.WEATHER, 5.0F, 1.0F);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getMainHandStack();
        if (user.isSneaking()) {
            user.setCurrentHand(hand);
            return TypedActionResult.consume(itemStack);
        }
        return super.use(world, user, hand);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (user instanceof PlayerEntity player && player.isSneaking()) {
            player.addVelocity(0, 2.0, 0);
            player.velocityModified = true;

         if(player instanceof PlayerEntityMixinAccessor accessor){
             accessor.fabric$setIgnoreFallDamage(true);
         }


        }
        super.onStoppedUsing(stack, world, user, remainingUseTicks);
    }
}