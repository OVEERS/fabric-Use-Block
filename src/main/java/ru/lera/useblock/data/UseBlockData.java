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

    public UseBlockData(int id, BlockPos pos, RegistryKey<World> dimension) {
        this.id = id;
        this.pos = pos;
        this.dimension = dimension;
    }

    // ==========================
    //   СЕРИАЛИЗАЦИЯ
    // ==========================
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();

        nbt.putInt("id", id);
        nbt.putLong("pos", pos.asLong());
        nbt.putString("dimension", dimension.getValue().toString());

        nbt.putString("text", Text.Serialization.toJsonString(text));
        nbt.putInt("inspectTime", inspectTime);
        nbt.putDouble("radius", radius);

        return nbt;
    }

    // ==========================
    //   ДЕСЕРИАЛИЗАЦИЯ
    // ==========================
    public static UseBlockData fromNbt(NbtCompound nbt) {

        int id = nbt.getInt("id");
        BlockPos pos = BlockPos.fromLong(nbt.getLong("pos"));

        RegistryKey<World> dim = RegistryKey.of(
                RegistryKeys.WORLD,
                new Identifier(nbt.getString("dimension"))
        );

        UseBlockData data = new UseBlockData(id, pos, dim);

        data.text = Text.Serialization.fromJson(nbt.getString("text"));
        data.inspectTime = nbt.getInt("inspectTime");
        data.radius = nbt.getDouble("radius");

        return data;
    }

}

