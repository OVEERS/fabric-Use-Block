package ru.lera.useblock.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

public class InspectionHud {
    private static boolean active = false;
    private static long startTime = 0;
    private static long duration = 0;
    private static int currentId = -1;
    private static long lastPlayedSecond = -1;
    private static BlockPos targetPos;
    private static double targetRadius;

    public static void start(int id, int seconds, BlockPos pos, double radius) {
        active = true;
        currentId = id;
        startTime = System.currentTimeMillis();
        duration = seconds * 1000L;
        targetPos = pos;
        targetRadius = radius;
        lastPlayedSecond = -1;
    }

    public static void render(DrawContext context, float tickDelta) {
        if (!active) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || targetPos == null) return;

        // ПРОВЕРКА ДИСТАНЦИИ (Прерывание)
        double distSq = client.player.getPos().squaredDistanceTo(targetPos.toCenterPos());
        if (distSq > (targetRadius * targetRadius) + 1.0) { // +1 для мягкости
            active = false;
            return;
        }

        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed >= duration) {
            active = false;
            UseBlockClientPackets.sendFinishInspection(currentId);
            return;
        }

        long elapsedSeconds = elapsed / 1000;
        if (elapsedSeconds != lastPlayedSecond) {
            client.player.playSound(net.minecraft.sound.SoundEvents.BLOCK_NOTE_BLOCK_BIT.value(), 0.4f, 1.2f);
            lastPlayedSecond = elapsedSeconds;
        }

        renderProgressBar(context, client, (float) elapsed / duration);
    }

    private static void renderProgressBar(DrawContext context, MinecraftClient client, float progress) {
        int w = client.getWindow().getScaledWidth();
        int h = client.getWindow().getScaledHeight();
        int barWidth = 120;
        int barHeight = 6;
        int x = w / 2 - barWidth / 2;
        int y = h / 2 + 50; // Чуть ниже центра экрана

        // Рамка и фон
        context.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xAA000000);
        context.fill(x, y, x + (int)(barWidth * progress), y + barHeight, 0xFFFFFFFF);

        // Заполнение полоски (плавный переход цвета от серого к белому)

        // Текст
        String text = "Идёт осмотр...";
        context.drawText(client.textRenderer, text, w / 2 - client.textRenderer.getWidth(text) / 2, y - 12, 0xFFFFFF, true);
    }
}