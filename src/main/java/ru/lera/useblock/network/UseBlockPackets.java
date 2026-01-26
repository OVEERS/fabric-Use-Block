package ru.lera.useblock.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import ru.lera.useblock.data.UseBlockState;
import ru.lera.useblock.data.UseBlockData;

public class UseBlockPackets {
    public static final Identifier START_INSPECTION = new Identifier("useblock", "start_inspection");
    public static final Identifier FINISH_INSPECTION = new Identifier("useblock", "finish_inspection");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(FINISH_INSPECTION, (server, player, handler, buf, responseSender) -> {
            int id = buf.readInt();
            server.execute(() -> {
                UseBlockState state = UseBlockState.get(player.getServerWorld());
                UseBlockData data = state.get(id);
                if (data != null) {
                    player.sendMessage(data.text, false);
                    player.removeStatusEffect(StatusEffects.SLOWNESS); // Снимаем замедление
                }
            });
        });
    }

    public static void sendStartInspection(ServerPlayerEntity player, UseBlockData data) {
        // Накладываем эффекты: Замедление (SLOWNESS) и Тьма (DARKNESS) для погружения
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, data.inspectTime * 20, 2, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 60, 0, false, false));

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(data.id);           // Исправлено: передаем int, а не объект
        buf.writeInt(data.inspectTime);  // Передаем время осмотра
        buf.writeBlockPos(data.pos);     // Передаем позицию для проверки дистанции
        buf.writeDouble(data.radius);    // Передаем радиус прерывания
        ServerPlayNetworking.send(player, START_INSPECTION, buf);
    }
}