package ru.lera.useblock.interaction;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import ru.lera.useblock.data.UseBlockState;
import ru.lera.useblock.data.UseBlockData;
import ru.lera.useblock.network.UseBlockPackets;

public class BlockUseHandler {

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;

            BlockHitResult bhr = (BlockHitResult) hitResult;
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            UseBlockState state = UseBlockState.get(serverPlayer.getServerWorld());

            UseBlockData data = state.find(bhr.getBlockPos(), world.getRegistryKey());
            if (data == null) return ActionResult.PASS;

            double distSq = player.getPos().squaredDistanceTo(data.pos.toCenterPos());
            if (distSq > (data.radius * data.radius)) {
                serverPlayer.sendMessage(Text.literal("§cСлишком далеко для изучения!"), true);
                return ActionResult.FAIL; // Отменяем действие полностью
            }
            if (data.requiredItem != null && !data.requiredItem.isEmpty()) {
                String heldItemId = net.minecraft.registry.Registries.ITEM.getId(player.getStackInHand(hand).getItem()).toString();
                if (!heldItemId.equals(data.requiredItem)) {
                    serverPlayer.sendMessage(Text.literal(data.lockMessage), true);
                    return ActionResult.FAIL;
                }
                if (!player.isCreative()) {
                    player.getStackInHand(hand).decrement(1);
                }
            }

            UseBlockPackets.sendStartInspection(serverPlayer, data);
            return ActionResult.SUCCESS;
        });
    }
}