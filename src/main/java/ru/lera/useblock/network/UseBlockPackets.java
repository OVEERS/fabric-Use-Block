package ru.lera.useblock.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import ru.lera.useblock.data.UseBlockState;
import ru.lera.useblock.data.UseBlockData;

import static ru.lera.useblock.UseBlockMod.LOGGER;

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
                    player.removeStatusEffect(StatusEffects.SLOWNESS);

                    if (data.command != null && !data.command.isEmpty()) {

                        ServerCommandSource source = server.getCommandSource()
                                .withLevel(4)
                                .withSilent();
                        // выполняю команду после таймера
                        server.getCommandManager().executeWithPrefix(source, data.command);

                        LOGGER.info("[UseBlock] Блок {} выполнил команду: /{}", data.id, data.command); // вывод в консоль команды
                    }
                }
            });
        });
    }

    public static void sendStartInspection(ServerPlayerEntity player, UseBlockData data) {
        // эффекты Замедление и Тьма для погружения
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, data.inspectTime * 20, 2, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 40, 0, false, false));

        ServerWorld serverWorld = player.getServerWorld();
        serverWorld.spawnParticles(ParticleTypes.CRIT,
                data.pos.getX() + 0.5, data.pos.getY() + 1.2, data.pos.getZ() + 0.5,
                10, 0.2, 0.2, 0.2, 0.05
        );

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(data.id);           // передача int
        buf.writeInt(data.inspectTime);  // передача времени осмотра
        buf.writeBlockPos(data.pos);     // передача позиции для проверки дистанции
        buf.writeDouble(data.radius);    // передача радиуса остановки
        ServerPlayNetworking.send(player, START_INSPECTION, buf);
    }
}