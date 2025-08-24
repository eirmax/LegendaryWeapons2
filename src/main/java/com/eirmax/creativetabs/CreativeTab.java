package com.eirmax.creativetabs;

import com.eirmax.LegendaryWeapons2;
import com.eirmax.item.ModItems;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CreativeTab {
    public static final ItemGroup LEGENDARY_WEAPONS = Registry.register(Registries.ITEM_GROUP,
            new Identifier(LegendaryWeapons2.MOD_ID, "legendaryweapons2_group"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.legendaryweapons2_group"))
                    .icon(() -> new ItemStack(ModItems.SWORDOFMIDAS)).entries((displayContext, entries) -> {
                        entries.add(ModItems.SWORDOFMIDAS);
                        entries.add(ModItems.SWORDOFWARDEN);
                        entries.add(ModItems.DRAGONKATANA);
                    }).build());




    public static void registerItemGroups(){}

}

