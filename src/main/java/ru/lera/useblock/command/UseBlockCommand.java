package ru.lera.useblock.command;

import com.mojang.authlib.yggdrasil.response.UserAttributesResponse;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.registry.Registries;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ru.lera.useblock.data.UseBlockData;
import ru.lera.useblock.data.UseBlockState;

import java.util.concurrent.ConcurrentNavigableMap;

public class UseBlockCommand {

    public static void register() {

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                CommandManager.literal("useblock")

                        // /useblock add
                        .then(CommandManager.literal("add")
                                .then(CommandManager.argument("id", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            int id = IntegerArgumentType.getInteger(context, "id");
                                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                                            HitResult hit = player.raycast(5.0, 0.0f, false);

                                            if (hit.getType() != HitResult.Type.BLOCK) return 0;
                                            BlockPos pos = ((BlockHitResult) hit).getBlockPos();

                                            UseBlockState state = UseBlockState.get(player.getServerWorld());
                                            UseBlockData data = state.get(id);

                                            if (data == null) {
                                                // создаю новый конфиг с этим ID
                                                data = new UseBlockData(id, pos, player.getWorld().getRegistryKey());
                                                data.positions.add(pos);
                                                state.blocks.put(id, data);
                                                context.getSource().sendFeedback(() -> Text.literal("§aСоздан новый ID " + id), false);
                                            } else {
                                                // добавляю блок к существующему ID
                                                if (!data.positions.contains(pos)) {
                                                    data.positions.add(pos);
                                                    context.getSource().sendFeedback(() -> Text.literal("§aБлок привязан к существующему ID " + id), false);
                                                }
                                            }
                                            state.markDirty();
                                            return 1;
                                        })
                                )
                        )

                        // /useblock text <id> <json>
                        .then(CommandManager.literal("text")
                                .then(CommandManager.argument("id", IntegerArgumentType.integer())
                                        .then(CommandManager.argument("json", StringArgumentType.greedyString())
                                                .executes(ctx -> {

                                                    int id = IntegerArgumentType.getInteger(ctx, "id");
                                                    String json = StringArgumentType.getString(ctx, "json");

                                                    ServerCommandSource src = ctx.getSource();
                                                    UseBlockState state = UseBlockState.get(src.getWorld());
                                                    UseBlockData data = state.get(id);

                                                    if (data == null) {
                                                        src.sendFeedback(() -> Text.literal("Блок с ID " + id + " не найден"), false);
                                                        return 0;
                                                    }

                                                    try {
                                                        data.text = Text.Serialization.fromJson(json);

                                                        src.sendFeedback(() -> Text.literal("Текст обновлён"), false);
                                                    } catch (Exception e) {
                                                        src.sendFeedback(() -> Text.literal("Ошибка JSON: " + e.getMessage()), false);
                                                    }

                                                    return 1;
                                                })
                                        ))
                        )

                        // /useblock command <id> <cmd>
                        .then(CommandManager.literal("command")
                                .then(CommandManager.argument("id", IntegerArgumentType.integer())
                                        .then(CommandManager.argument("command", StringArgumentType.greedyString())
                                                .executes(context -> {
                                                    int id = IntegerArgumentType.getInteger(context, "id");
                                                    String cmd = StringArgumentType.getString(context, "command");

                                                    ServerCommandSource source = context.getSource();
                                                    UseBlockState state = UseBlockState.get(source.getWorld());
                                                    UseBlockData data = state.get(id);

                                                    if (data != null) {
                                                        data.command = cmd;
                                                        state.markDirty();
                                                        source.sendFeedback(() -> Text.literal("Команда для ID " + id + " установлена: " + cmd), false);
                                                    } else {
                                                        source.sendError(Text.literal("Блок с ID " + id + " не найден!"));
                                                    }
                                                    return 1;
                                                })
                                        )
                                )
                        )

                        // /useblock inspect_time <id> <seconds>
                        .then(CommandManager.literal("inspect_time")
                                .then(CommandManager.argument("id", IntegerArgumentType.integer())
                                        .then(CommandManager.argument("seconds", IntegerArgumentType.integer())
                                                .executes(context -> {

                                                    int id = IntegerArgumentType.getInteger(context, "id");
                                                    int seconds = IntegerArgumentType.getInteger(context, "seconds");

                                                    ServerCommandSource source = context.getSource();
                                                    UseBlockState state = UseBlockState.get(source.getWorld());

                                                    UseBlockData data = state.get(id);
                                                    if (data == null) {
                                                        source.sendError(Text.literal("Блок с ID " + id + " не найден"));
                                                        return 0;
                                                    }

                                                    data.inspectTime = seconds;
                                                    state.markDirty();

                                                    source.sendFeedback(
                                                            () -> Text.literal("inspectTime блока " + id + " = " + seconds),
                                                            false
                                                    );

                                                    return 1;
                                                })
                                        )
                                )
                        )

                        // /useblock radius <id> <value>
                        .then(CommandManager.literal("radius")
                                .then(CommandManager.argument("id", IntegerArgumentType.integer())
                                        .then(CommandManager.argument("value", DoubleArgumentType.doubleArg())
                                                .executes(context -> {

                                                    int id = IntegerArgumentType.getInteger(context, "id");
                                                    double value = DoubleArgumentType.getDouble(context, "value");

                                                    ServerCommandSource source = context.getSource();
                                                    UseBlockState state = UseBlockState.get(source.getWorld());

                                                    UseBlockData data = state.get(id);
                                                    if (data == null) {
                                                        source.sendError(Text.literal("Блок с ID " + id + " не найден"));
                                                        return 0;
                                                    }

                                                    data.radius = value;
                                                    state.markDirty();

                                                    source.sendFeedback(
                                                            () -> Text.literal("radius блока " + id + " = " + value),
                                                            false
                                                    );

                                                    return 1;
                                                })
                                        )
                                )
                        )

                        // /useblock remove <id>
                        .then(CommandManager.literal("remove")
                                .then(CommandManager.argument("id", IntegerArgumentType.integer())
                                        .executes(context -> {

                                            int id = IntegerArgumentType.getInteger(context, "id");

                                            ServerCommandSource source = context.getSource();
                                            UseBlockState state = UseBlockState.get(source.getWorld());

                                            if (state.remove(id)) {
                                                context.getSource().sendFeedback(() -> Text.translatable("useblock.command.removed", id), false); // шлю подтверждение
                                            } else {
                                                context.getSource().sendFeedback(() -> Text.translatable("useblock.command.not_found"), false); // шлю ошибку
                                            }

                                            return 1;
                                        })
                                )
                        )

                        // /useblock info <id>
                        .then(CommandManager.literal("info")
                                .then(CommandManager.argument("id", IntegerArgumentType.integer())
                                        .executes(context -> {

                                            int id = IntegerArgumentType.getInteger(context, "id");

                                            ServerCommandSource source = context.getSource();
                                            UseBlockState state = UseBlockState.get(source.getWorld());

                                            UseBlockData data = state.get(id);
                                            if (data == null) {
                                                source.sendError(Text.literal("Блок с ID " + id + " не найден"));
                                                return 0;
                                            }

                                            source.sendFeedback(
                                                    () -> Text.literal(
                                                            "ID: " + data.id +
                                                                    "\nPos: " + data.pos.toShortString() +
                                                                    "\nDim: " + data.dimension.getValue() +
                                                                    "\ninspectTime: " + data.inspectTime +
                                                                    "\nradius: " + data.radius +
                                                                    "\ntext: " + Text.Serialization.toJsonString(data.text) +
                                                                    "\nКлюч (Item): " + (data.requiredItem.isEmpty() ? "НЕТ" : data.requiredItem) +
                                                                    "\nMsg: " + data.lockMessage +
                                                                    "\nPos: " + data.pos.toShortString()
                                                    ),
                                                    false
                                            );

                                            return 1;
                                        })
                                )
                        )

                        // /useblock list
                        .then(CommandManager.literal("list")
                                .executes(context -> {

                                    ServerCommandSource source = context.getSource();
                                    UseBlockState state = UseBlockState.get(source.getWorld());

                                    if (state.blocks.isEmpty()) {
                                        source.sendFeedback(() -> Text.literal("Нет зарегистрированных блоков"), false);
                                        return 1;
                                    }

                                    StringBuilder sb = new StringBuilder("Список ID:\n");

                                    for (int id : state.blocks.keySet()) {
                                        sb.append(id).append("\n");
                                    }

                                    source.sendFeedback(() -> Text.literal(sb.toString()), false);

                                    return 1;
                                })
                        )
                        // /useblock key <id> <object>
                        .then(CommandManager.literal("key")
                                .then(CommandManager.argument("id", IntegerArgumentType.integer())
                                        .then(CommandManager.argument("item", ItemStackArgumentType.itemStack(registryAccess))
                                                .executes(context -> {
                                                    int id = IntegerArgumentType.getInteger(context, "id");
                                                    // Получаем ID предмета через специальный тип аргумента
                                                    var itemInput = ItemStackArgumentType.getItemStackArgument(context, "item");
                                                    String itemId = Registries.ITEM.getId(itemInput.getItem()).toString();

                                                    UseBlockState state = UseBlockState.get(context.getSource().getWorld());
                                                    UseBlockData data = state.get(id);
                                                    if (data != null) {
                                                        data.requiredItem = itemId;
                                                        state.markDirty();
                                                        context.getSource().sendFeedback(() -> Text.literal("§aБлок " + id + " теперь требует: §6" + itemId), false);
                                                    }
                                                    return 1;
                                                })
                                        )
                                )
                        )

                        // /useblock lock_msg <id> <text>
                        .then(CommandManager.literal("lock_msg")
                                .then(CommandManager.argument("id", IntegerArgumentType.integer())
                                        .then(CommandManager.argument("msg", StringArgumentType.greedyString())
                                                .executes(context -> {
                                                    int id = IntegerArgumentType.getInteger(context, "id");
                                                    String msg = StringArgumentType.getString(context, "msg").replace("&", "§");
                                                    UseBlockState state = UseBlockState.get(context.getSource().getWorld());
                                                    UseBlockData data = state.get(id);

                                                    if (data != null) {
                                                        data.lockMessage = msg;
                                                        state.markDirty();
                                                        context.getSource().sendFeedback(() -> Text.translatable("useblock.command.msg_updated"), false); // шлю перевод
                                                    }
                                                    return 1;
                                                })
                                        )
                                )
                        )

                        .then(CommandManager.literal("setup")
                                .then(CommandManager.argument("id", IntegerArgumentType.integer())
                                        .then(CommandManager.argument("seconds", IntegerArgumentType.integer(0, 300))
                                                .executes(context -> {
                                                    int id = IntegerArgumentType.getInteger(context, "id"); // беру id блока
                                                    int seconds = IntegerArgumentType.getInteger(context, "seconds"); // беру время
                                                    UseBlockState state = UseBlockState.get(context.getSource().getWorld()); // беру данные мира
                                                    UseBlockData data = state.get(id); // ищу блок по id

                                                    if (data != null) {
                                                        data.inspectTime = seconds;

                                                        // если стоит вечный замок, то мы его снимаем
                                                        if (data.requiredItem.equalsIgnoreCase("locked")) {
                                                            data.requiredItem = ""; // снимаю замок
                                                        }

                                                        state.markDirty();
                                                        // вывожу ебейший текст
                                                        context.getSource().sendFeedback(() -> Text.literal("Время осмотра для ID " + id + " установлен на " + seconds + " сек."), false);
                                                    }
                                                    else {
                                                        context.getSource().sendError(Text.literal("Блок с ID " + id + " не найден"));
                                                    }
                                                    return 1;
                                                })
                                        )
                                )
                        )

                        // / useblock lock id Вечный замок
                        .then(CommandManager.literal("lock")
                                .then(CommandManager.argument("id", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            int id = IntegerArgumentType.getInteger(context, "id");
                                            UseBlockState state = UseBlockState.get(context.getSource().getWorld());
                                            UseBlockData data = state.get(id);

                                            if (data != null) {
                                                data.requiredItem = "locked";
                                                data.inspectTime = 0;
                                                state.markDirty();
                                                context.getSource().sendFeedback(() -> Text.literal("§6[UseBlock] §fБлок ID " + id + " теперь §cху откроешь." ), false);
                                            }
                                            else {
                                                context.getSource().sendError(Text.literal("Блок с ID " + id + " не найден"));
                                            }
                                            return 1;
                                        })
                                )
                        )

                        // /useblock unlock id
                        .then(CommandManager.literal("unlock")
                                .then(CommandManager.argument("id", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            int id = IntegerArgumentType.getInteger(context, "id");
                                            UseBlockState state = UseBlockState.get(context.getSource().getWorld());
                                            UseBlockData data = state.get(id);

                                            if (data != null) {
                                                data.requiredItem = "";
                                                data.inspectTime = 0;
                                                state.markDirty();
                                                context.getSource().sendFeedback(() -> Text.literal("§6[UseBlock] §fБлок ID " + id + " теперь §cоткрывается."), false);
                                            }
                                            else {
                                                context.getSource().sendError(Text.literal("Блок с ID " + id + " не найден"));
                                            }
                                            return 1;
                                        })
                                )
                        )
                )
        );


    }
}
