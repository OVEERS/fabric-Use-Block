package ru.lera.useblock.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class UseBlockState extends PersistentState {

    public final Map<Integer, UseBlockData> blocks = new HashMap<>();
    private int nextId = 1;

    //   PersistentState TYPE
    public static final Type<UseBlockState> TYPE = new Type<>(
            UseBlockState::new,
            UseBlockState::fromNbt,
            null
    );

    public static final String KEY = "useblock_data";

    public UseBlockState() {}

    //   СОЗДАНИЕ НОВОГО БЛОКА
    public int create(BlockPos pos, RegistryKey<World> dim) {
        int id = nextId++;

        UseBlockData data = new UseBlockData(id, pos, dim);
        blocks.put(id, data);

        markDirty();
        return id;
    }

    //   ПОЛУЧЕНИЕ ПО ID
    public UseBlockData get(int id) {
        return blocks.get(id);
    }

    //   ПОИСК ПО ПОЗИЦИИ
    public UseBlockData find(BlockPos pos, RegistryKey<World> dim) {
        for (UseBlockData data : blocks.values()) {
            if (data.dimension.equals(dim) && data.positions.contains(pos)) {
                return data;
            }
        }
        return null;
    }

    //   УДАЛЕНИЕ
    public boolean remove(int id) {
        if (blocks.remove(id) != null) {
            markDirty();
            return true;
        }
        return false;
    }

    //   СЕРИАЛИЗАЦИЯ
    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {

        nbt.putInt("nextId", nextId);

        NbtList list = new NbtList();
        for (UseBlockData data : blocks.values()) {
            list.add(data.toNbt());
        }

        nbt.put("blocks", list);
        return nbt;
    }

    //   ДЕСЕРИАЛИЗАЦИЯ
    public static UseBlockState fromNbt(NbtCompound nbt) {

        UseBlockState state = new UseBlockState();

        state.nextId = nbt.getInt("nextId");

        NbtList list = nbt.getList("blocks", 10);
        for (int i = 0; i < list.size(); i++) {

            UseBlockData data = UseBlockData.fromNbt(list.getCompound(i));

            if (data.id > 0) {
                state.blocks.put(data.id, data);
            }
        }

        // Пересчёт nextId
        state.nextId = state.blocks.keySet().stream()
                .mapToInt(v -> v)
                .max()
                .orElse(0) + 1;

        state.markDirty();
        return state;
    }


    //   ПОЛУЧЕНИЕ ИЗ МИРА
    public static UseBlockState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                TYPE,
                KEY
        );
    }

    public int addBlock(UseBlockData data) {
        return 0;
    }
}
