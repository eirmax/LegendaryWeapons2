package com.eirmax.item.custom;

import com.eirmax.material.SwordOfMidasTier;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SwordOfMidas extends SwordItem {

    private static final int COOLDOWN_TICKS = 450;
    private static final int FIRE_RADIUS = 20;
    private static final int FIRE_DURATION = 10 * 20;

    private static final Map<UUID, Long> BURNING_PLAYERS = new HashMap<>();

    public SwordOfMidas(Settings settings) {
        super(new SwordOfMidasTier(), 0, -2.4f, settings.fireproof());
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.getWorld().isClient && target.isDead() && target instanceof PlayerEntity) {
            NbtCompound nbt = stack.getOrCreateNbt();
            if (!nbt.contains("MidasKills")) {
                nbt.putInt("MidasKills", 0);
            }

            int kills = nbt.getInt("MidasKills");
            kills++;
            nbt.putInt("MidasKills", kills);
            nbt.putBoolean("Unbreakable", true);
            updateSharpnessEnchantment(stack, kills);
        }

        return super.postHit(stack, target, attacker);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }

        if (!world.isClient) {
            ignitePlayersInRadius((ServerWorld) world, user, FIRE_RADIUS);
            user.getItemCooldownManager().set(this, COOLDOWN_TICKS);
        }

        return TypedActionResult.success(stack);
    }

    private void ignitePlayersInRadius(ServerWorld world, PlayerEntity user, double radius) {
        Box box = new Box(
                user.getX() - radius, user.getY() - radius, user.getZ() - radius,
                user.getX() + radius, user.getY() + radius, user.getZ() + radius
        );

        List<PlayerEntity> players = world.getEntitiesByClass(PlayerEntity.class, box, p -> p != user);

        for (PlayerEntity player : players) {
            player.setOnFireFor(10);
            BURNING_PLAYERS.put(player.getUuid(), world.getTime() + FIRE_DURATION);
            world.spawnParticles(ParticleTypes.FLAME,
                    player.getX(), player.getY(), player.getZ(),
                    20, 0.5, 1.0, 0.5, 0.1);
        }

        world.playSound(null, user.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE,
                SoundCategory.PLAYERS, 2.0f, 0.8f);
    }

    public static void handleBurningPlayers(ServerWorld world) {
        long currentTime = world.getTime();

        BURNING_PLAYERS.entrySet().removeIf(entry -> entry.getValue() < currentTime);

        for (UUID playerId : BURNING_PLAYERS.keySet()) {
            PlayerEntity player = world.getPlayerByUuid(playerId);
            if (player != null) {
                BlockPos pos = player.getBlockPos();

                if (world.getBlockState(pos).isAir()) {
                    world.setBlockState(pos, Blocks.FIRE.getDefaultState());
                }

                world.spawnParticles(ParticleTypes.FLAME,
                        player.getX(), player.getY(), player.getZ(),
                        5, 0.3, 0.1, 0.3, 0.05);
            }
        }
    }

    private void updateSharpnessEnchantment(ItemStack stack, int kills) {
        NbtCompound nbt = stack.getOrCreateNbt();
        NbtList enchantments = nbt.getList("Enchantments", 10);

        for (int i = enchantments.size() - 1; i >= 0; i--) {
            NbtCompound enchantment = enchantments.getCompound(i);
            if (enchantment.getString("id").equals("minecraft:sharpness")) {
                enchantments.remove(i);
            }
        }

        int sharpnessLevel = 5 + (int) (kills * 0.5);
        if (sharpnessLevel < 1) sharpnessLevel = 1;

        NbtCompound newEnchantment = new NbtCompound();
        newEnchantment.putString("id", "minecraft:sharpness");
        newEnchantment.putShort("lvl", (short) sharpnessLevel);
        enchantments.add(newEnchantment);
        nbt.put("Enchantments", enchantments);
    }


    @Override
    public void onCraft(ItemStack stack, World world, PlayerEntity player) {
        super.onCraft(stack, world, player);

        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putInt("MidasKills", 0);
        nbt.putBoolean("Unbreakable", true);

        stack.addEnchantment(Enchantments.SHARPNESS, 5);
    }


    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains("MidasKills")) {
            int kills = nbt.getInt("MidasKills");
            float bonusDamage = kills * 0.5f;
            float totalDamage = 9.0f + bonusDamage;

            tooltip.add(Text.literal("Количество убийств: " + kills).formatted(Formatting.DARK_RED));
        }

        tooltip.add(Text.literal("Выкован поднебесным кузнецом по приказу царя Мидаса").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("При убийстве игрока мечом увеличивает его остроту на 0.5").formatted(Formatting.GRAY));
    }


    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isDamageable() {
        return false;
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return false;
    }
}