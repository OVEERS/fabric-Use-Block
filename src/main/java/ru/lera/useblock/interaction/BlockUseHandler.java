package ru.lera.useblock.interaction;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.server.network.ServerPlayerEntity;
import ru.lera.useblock.data.UseBlockState;
import ru.lera.useblock.data.UseBlockData;
import ru.lera.useblock.network.UseBlockPackets;

public class BlockUseHandler {

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS; // Проверка руки

            BlockHitResult bhr = (BlockHitResult) hitResult;
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            UseBlockState state = UseBlockState.get(serverPlayer.getServerWorld());

            UseBlockData data = state.find(bhr.getBlockPos(), world.getRegistryKey());
            if (data == null) return ActionResult.PASS;

            UseBlockPackets.sendStartInspection(serverPlayer, data);

            return ActionResult.SUCCESS;
        });
    }
}