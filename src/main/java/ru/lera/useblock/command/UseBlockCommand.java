package ru.lera.useblock.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ru.lera.useblock.data.UseBlockData;
import ru.lera.useblock.data.UseBlockState;

public class UseBlockCommand {

    public static void register() {

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                CommandManager.literal("useblock")

                        // ============================
                        // /useblock add
                        // ============================
                        .then(CommandManager.literal("add")
                                .executes(context -> {

                                    ServerCommandSource source = context.getSource();

                                    if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
                                        source.sendError(Text.literal("Команду может использовать только игрок"));
                                        return 0;
                                    }

                                    HitResult hit = player.raycast(5.0, 0.0f, false);

                                    if (hit.getType() != HitResult.Type.BLOCK) {
                                        source.sendError(Text.literal("Ты не смотришь на блок"));
                                        return 0;
                                    }

                                    BlockHitResult blockHit = (BlockHitResult) hit;

                                    ServerWorld world = player.getServerWorld();
                                    UseBlockState state = UseBlockState.get(world);

                                    BlockPos pos = blockHit.getBlockPos();
                                    RegistryKey<World> dim = world.getRegistryKey();

                                    int id = state.create(pos, dim);

                                    source.sendFeedback(
                                            () -> Text.literal(
                                                    "[UseBlock] Блоку \"" +
                                                            world.getBlockState(pos).getBlock().getName().getString() +
                                                            "\" по координатам " +
                                                            pos.toShortString() +
                                                            " присвоен ID: " + id
                                            ),
                                            false
                                    );

                                    return 1;
                                })
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

                        // ============================
                        // /useblock command <id> <cmd>
                        // ============================
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

                        // ============================
                        // /useblock inspect_time <id> <seconds>
                        // ============================
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

                        // ============================
                        // /useblock radius <id> <value>
                        // ============================
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

                        // ============================
                        // /useblock remove <id>
                        // ============================
                        .then(CommandManager.literal("remove")
                                .then(CommandManager.argument("id", IntegerArgumentType.integer())
                                        .executes(context -> {

                                            int id = IntegerArgumentType.getInteger(context, "id");

                                            ServerCommandSource source = context.getSource();
                                            UseBlockState state = UseBlockState.get(source.getWorld());

                                            if (state.remove(id)) {
                                                source.sendFeedback(
                                                        () -> Text.literal("Блок " + id + " удалён"),
                                                        false
                                                );
                                            } else {
                                                source.sendError(Text.literal("Блок с ID " + id + " не найден"));
                                            }

                                            return 1;
                                        })
                                )
                        )

                        // ============================
                        // /useblock info <id>
                        // ============================
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
                                                                    "\ntext: " + Text.Serialization.toJsonString(data.text)
                                                    ),
                                                    false
                                            );

                                            return 1;
                                        })
                                )
                        )

                        // ============================
                        // /useblock list
                        // ============================
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
                        .then(CommandManager.literal("key")
                                .then(CommandManager.argument("id", IntegerArgumentType.integer())
                                        .then(CommandManager.argument("item", StringArgumentType.string())
                                                .executes(context -> {
                                                    int id = IntegerArgumentType.getInteger(context, "id");
                                                    String item = StringArgumentType.getString(context, "item");
                                                    UseBlockState state = UseBlockState.get(context.getSource().getWorld());
                                                    UseBlockData data = state.get(id);
                                                    if (data != null) {
                                                        data.requiredItem = item;
                                                        state.markDirty();
                                                        context.getSource().sendFeedback(() -> Text.literal("Для блока " + id + " нужен предмет: " + item), false);
                                                    }
                                                    return 1;
                                                })
                                        )
                                )
                        )
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
                                                        context.getSource().sendFeedback(() -> Text.literal("Сообщение об ошибке обновлено"), false);
                                                    }
                                                    return 1;
                                                })
                                        )
                                )
                        )
        ));
    }
}
