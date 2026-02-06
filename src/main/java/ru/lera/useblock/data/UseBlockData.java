package ru.lera.useblock.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UseBlockData {

    public int id;
    public BlockPos pos;
    public RegistryKey<World> dimension;

    public Text text = Text.literal("Пустое описание");
    public int inspectTime = 5;
    public double radius = 2.0;
    public String command = "";
    public String requiredItem = "";
    public String lockMessage = "§cВам нужен определенный предмет!";

    public UseBlockData(int id, BlockPos pos, RegistryKey<World> dimension) {
        this.id = id;
        this.pos = pos;
        this.dimension = dimension;
    }

    public java.util.List<BlockPos> positions = new java.util.ArrayList<>(); // список всех привязанных блоков

    //   СЕРИАЛИЗАЦИЯ в nbt
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("id", id);

        net.minecraft.nbt.NbtList posList = new net.minecraft.nbt.NbtList();
        for (BlockPos p : positions) {
            posList.add(net.minecraft.nbt.NbtLong.of(p.asLong()));
        }
        nbt.putString("dimension", dimension.getValue().toString());
        nbt.putString("text", Text.Serialization.toJsonString(text));
        nbt.putInt("inspectTime", inspectTime);
        nbt.putDouble("radius", radius);
        nbt.putString("command", command);
        nbt.putString("requiredItem", requiredItem);
        nbt.putString("lockMessage", lockMessage);
        return nbt;
    }


    //   ДЕСЕРИАЛИЗАЦИЯ из nbt
    public static UseBlockData fromNbt(NbtCompound nbt) {
        int id = nbt.getInt("id");
        RegistryKey<World> dim = RegistryKey.of(RegistryKeys.WORLD, new Identifier(nbt.getString("dimension")));
        UseBlockData data = new UseBlockData(id, null, dim);

        net.minecraft.nbt.NbtList posList = nbt.getList("positions", 4); // 4 — это тип Long
        for (int i = 0; i < posList.size(); i++) {
            // Сначала получаем элемент, проверяем, что это NbtLong, и берем значение
            net.minecraft.nbt.NbtElement element = posList.get(i);
            if (element instanceof net.minecraft.nbt.NbtLong nbtLong) {
                data.positions.add(BlockPos.fromLong(nbtLong.longValue()));
            }
        }

        data.text = Text.Serialization.fromJson(nbt.getString("text"));
        data.inspectTime = nbt.getInt("inspectTime");
        data.radius = nbt.getDouble("radius");
        data.command = nbt.getString("command");
        data.requiredItem = nbt.getString("requiredItem");
        data.lockMessage = nbt.getString("lockMessage");
        return data;
    }

}

