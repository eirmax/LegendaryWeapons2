package com.eirmax.item.custom;

import com.eirmax.material.SwordOfMidasTier;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;

public class SwordOfMidas extends SwordItem {

    public SwordOfMidas(Settings settings) {
        super(new SwordOfMidasTier(), 0, -2.4f, settings.fireproof());
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.getWorld().isClient && target.isDead()) {
            NbtCompound nbt = stack.getOrCreateNbt();
            if (!nbt.contains("MidasKills")) {
                nbt.putInt("MidasKills", 0);
            }

            int kills = nbt.getInt("MidasKills");
            kills++;
            nbt.putInt("MidasKills", kills);
            nbt.putBoolean("Unbreakable", true);
            updateSharpnessEnchantment(stack, kills);

            if (attacker instanceof PlayerEntity player) {
                float bonusDamage = kills * 0.5f;
                float totalDamage = 9.0f + bonusDamage;
                player.sendMessage(
                        Text.literal("Меч Мидаса поглотил душу! Бонусный урон: +" + String.format("%.1f", bonusDamage))
                                .formatted(Formatting.GOLD),
                        true
                );
            }
        }

        return super.postHit(stack, target, attacker);
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

        int sharpnessLevel = 5 + (int)(kills * 0.5);
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
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, net.minecraft.client.item.TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains("MidasKills")) {
            int kills = nbt.getInt("MidasKills");
            float bonusDamage = kills * 0.5f;
            float totalDamage = 9.0f + bonusDamage;

            tooltip.add(Text.literal("Души поглощено: " + kills).formatted(Formatting.DARK_RED));
        }

        tooltip.add(Text.literal("Неразрушимый и огнестойкий").formatted(Formatting.GOLD));
        tooltip.add(Text.literal("Легендарный меч царя Мидаса").formatted(Formatting.YELLOW));
        tooltip.add(Text.literal("Каждое убийство: +0.5 урона").formatted(Formatting.AQUA));
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