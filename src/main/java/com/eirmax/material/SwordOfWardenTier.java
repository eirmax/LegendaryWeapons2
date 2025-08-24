package com.eirmax.material;


import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public class SwordOfWardenTier implements ToolMaterial {
    @Override
    public int getDurability() {
        return -1;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return 4.0f;
    }

    @Override
    public float getAttackDamage() {
        return 8.0f;
    }

    @Override
    public int getMiningLevel() {
        return 4;
    }

    @Override
    public int getEnchantability() {
        return 5;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.EMPTY;
    }
}