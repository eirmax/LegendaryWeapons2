package com.eirmax.packets;

import com.eirmax.packet.PoseidonStrikePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

public class PoseidonStrikePacketClient {

    public static void send() {
        ClientPlayNetworking.send(PoseidonStrikePacket.ID, PacketByteBufs.empty());
    }
}
