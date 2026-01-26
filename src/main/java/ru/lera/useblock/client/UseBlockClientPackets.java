package ru.lera.useblock.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;


import ru.lera.useblock.network.UseBlockPackets;

public class UseBlockClientPackets {

    public static void sendFinishInspection(int id) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(id);
        ClientPlayNetworking.send(UseBlockPackets.FINISH_INSPECTION, buf);
    }

    public static void register() {
    }
}
