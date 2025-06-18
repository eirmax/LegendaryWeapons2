package com.eirmax;

import com.eirmax.keybind.TridentKeyBind;
import net.fabricmc.api.ClientModInitializer;

public class LegendaryWeapons2Client implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		TridentKeyBind.registerKeyBinds();
	}
}