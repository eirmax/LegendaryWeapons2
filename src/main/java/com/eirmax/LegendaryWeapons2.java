package com.eirmax;

import com.eirmax.packet.PoseidonStrikePacket;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegendaryWeapons2 implements ModInitializer {
	public static final String MOD_ID = "legendaryweapons2";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {


		PoseidonStrikePacket.registerReceiver();
	}
}