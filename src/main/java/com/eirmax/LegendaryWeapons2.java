package com.eirmax;

import com.eirmax.event.DragonKatanaEventHandler;
import com.eirmax.event.SwordOfMidasEventHandler;
import com.eirmax.item.ModItems;
import com.eirmax.event.SwordOfWardenEventHandler;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegendaryWeapons2 implements ModInitializer {
	public static final String MOD_ID = "legendaryweapons2";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {


		DragonKatanaEventHandler.init();
		ModItems.registerModItems();
		SwordOfWardenEventHandler.init();
		SwordOfMidasEventHandler.init();

	}
}