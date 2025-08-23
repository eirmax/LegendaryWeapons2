package com.eirmax.material;

import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public class SwordOfMidasTier implements ToolMaterial {
    @Override
    public int getDurability() {
        return -1;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return 12.0f;
    }

    @Override
    public float getAttackDamage() {
        return 9.0f;
    }

    @Override
    public int getMiningLevel() {
        return 4;
    }

    @Override
    public int getEnchantability() {
        return 22;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.EMPTY;
    }
}