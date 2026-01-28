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

            if (data.requiredItem != null && !data.requiredItem.trim().isEmpty() && !data.requiredItem.equalsIgnoreCase("none")) {

                var stack = player.getStackInHand(hand);
                // Получаем ID предмета в руке (например, "minecraft:iron_axe")
                String heldItemId = net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString();

                // ГЛАВНАЯ ПРОВЕРКА:
                // 1. Если рука пустая, heldItemId будет "minecraft:air"
                // 2. Сравниваем ID предмета с тем, что сохранено в данных
                if (!heldItemId.equals(data.requiredItem)) {
                    // Если ID не совпали — выводим сообщение в экшн-бар
                    serverPlayer.sendMessage(Text.literal(data.lockMessage), true);
                    return ActionResult.FAIL;
                }

                // Если мы здесь — ключ подошел!
                if (!player.isCreative()) {
                    stack.decrement(1); // Забираем предмет, если не креатив
                }
            }

            UseBlockPackets.sendStartInspection(serverPlayer, data);
            return ActionResult.SUCCESS;
        });
    }
}