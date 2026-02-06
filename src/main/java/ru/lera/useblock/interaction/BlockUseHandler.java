package ru.lera.useblock.interaction;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import ru.lera.useblock.data.UseBlockState;
import ru.lera.useblock.data.UseBlockData;
import ru.lera.useblock.network.UseBlockPackets;

public class BlockUseHandler {

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            // 0. Базовые проверки
            if (world.isClient()) return ActionResult.PASS;
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS; // Обрабатываем только основную руку

            BlockHitResult bhr = (BlockHitResult) hitResult;
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            UseBlockState state = UseBlockState.get(serverPlayer.getServerWorld());

            UseBlockData data = state.find(bhr.getBlockPos(), world.getRegistryKey());
            if (data == null) return ActionResult.PASS;

            // 1. Проверка дистанции (только для блоков с осмотром)
            if (data.inspectTime > 0) {
                double distSq = player.getPos().squaredDistanceTo(bhr.getBlockPos().toCenterPos());
                if (distSq > (data.radius * data.radius)) {
                    serverPlayer.sendMessage(Text.literal("§cСлишком далеко!"), true);
                    return ActionResult.FAIL;
                }
            }

            // 2. Логика ключа
            boolean canOpen = true; // По умолчанию можно, если нет требований

            if (data.requiredItem != null && !data.requiredItem.trim().isEmpty() && !data.requiredItem.equalsIgnoreCase("none")) {

                // Проверка на вечный замок
                if (data.requiredItem.equalsIgnoreCase("locked")) {
                    serverPlayer.sendMessage(Text.literal(data.lockMessage), true);
                    return ActionResult.FAIL;
                }

                var stack = player.getStackInHand(hand);
                String heldItemId = net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString();

                if (!heldItemId.equals(data.requiredItem)) {
                    // КЛЮЧ НЕ ТОТ
                    serverPlayer.sendMessage(Text.literal(data.lockMessage), true);
                    return ActionResult.FAIL; // Блокируем открытие
                } else {
                    // КЛЮЧ ТОТ
                    canOpen = true;
                    if (!player.isCreative()) {
                        stack.decrement(1); // Забираем ключ
                    }
                }
            }

            // 3. Финальное действие
            if (canOpen) {
                if (data.inspectTime <= 0) {
                    // Если время 0, возвращаем PASS, чтобы ванильная дверь открылась сама
                    return ActionResult.PASS;
                } else {
                    // Если время > 0, запускаем полоску
                    UseBlockPackets.sendStartInspection(serverPlayer, data);
                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.FAIL;
        });
    }
}