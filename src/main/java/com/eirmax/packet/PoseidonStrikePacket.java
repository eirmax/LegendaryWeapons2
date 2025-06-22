package com.eirmax.packet;

import com.eirmax.LegendaryWeapons2;
import com.eirmax.item.ModItems;
import com.eirmax.item.cutom.PoseidonsTrident;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class PoseidonStrikePacket {
    public static final Identifier ID = new Identifier(LegendaryWeapons2.MOD_ID, "poseidon_strike");
    public static void registerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                if (player.getMainHandStack().getItem() == ModItems.POSEIDONSTRIDENT) {
                    PoseidonsTrident.strikeLightningAOE(player);
                }
            });
        });
    }
}