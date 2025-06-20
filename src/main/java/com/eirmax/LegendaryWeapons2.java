package com.eirmax;

import com.eirmax.item.ModItems;
import com.eirmax.packet.PoseidonStrikePacket;
import com.eirmax.utils.SwordOfWardenHandler;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegendaryWeapons2 implements ModInitializer {
	public static final String MOD_ID = "legendaryweapons2";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {


		ModItems.registerModItems();
		PoseidonStrikePacket.registerReceiver();
		SwordOfWardenHandler.init();
	}
}