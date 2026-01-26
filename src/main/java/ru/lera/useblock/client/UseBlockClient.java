package ru.lera.useblock.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.util.math.BlockPos;
import ru.lera.useblock.network.UseBlockPackets;

public class UseBlockClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        UseBlockClientPackets.register();

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            InspectionHud.render(drawContext, tickDelta);
        });

        ClientPlayNetworking.registerGlobalReceiver(
                UseBlockPackets.START_INSPECTION,
                (client, handler, buf, responseSender) -> {
                    int id = buf.readInt();
                    int seconds = buf.readInt();
                    BlockPos pos = buf.readBlockPos();
                    double radius = buf.readDouble();

                    client.execute(() -> {
                        InspectionHud.start(id, seconds, pos, radius);
                    });
                }
        );
    }
}