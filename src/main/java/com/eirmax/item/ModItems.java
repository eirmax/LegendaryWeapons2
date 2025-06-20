package com.eirmax.item;

import com.eirmax.LegendaryWeapons2;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {



    public static final Item POSEIDONSTRIDENT = registerItem("poseidonstrident", new PoseidonsTrident(new Item.Settings().maxCount(1)));
    public static final Item SWORDOFWARDEN = registerItem("sword_of_warden", new SwordOfWarden(new Item.Settings().maxCount(1)));
    public static final Item ENDERBRAID = registerItem("ender_braid", new SwordOfWarden(new Item.Settings().maxCount(1)));



    public static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(LegendaryWeapons2.MOD_ID, name), item);
    }

    public static void registerModItems() {
        LegendaryWeapons2.LOGGER.info("Registering Mod Items for " + LegendaryWeapons2.MOD_ID);
    }

}
