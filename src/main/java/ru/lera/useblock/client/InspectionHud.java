package ru.lera.useblock.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;

public class InspectionHud {

    private static boolean active = false;
    private static long startTime = 0;
    private static long duration = 0;
    private static int currentId = -1;

    private static BlockPos targetPos;
    private static double targetRadius;

    public static void start(int id, int seconds, BlockPos pos, double radius) {
        active = true;
        currentId = id;
        startTime = System.currentTimeMillis();
        duration = seconds * 1000L;
        targetPos = pos;
        targetRadius = radius;
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

        renderProgressBar(context, client, (float) elapsed / duration);
    }

    private static void renderProgressBar(DrawContext context, MinecraftClient client, float progress) {
        int w = client.getWindow().getScaledWidth();
        int h = client.getWindow().getScaledHeight();

        int barWidth = 140;
        int barHeight = 8;
        int x = w / 2 - barWidth / 2;
        int y = h / 2 + 40; // Чуть ниже центра экрана

        // Рамка и фон
        context.fill(x - 2, y - 2, x + barWidth + 2, y + barHeight + 2, 0xAA000000);

        // Заполнение полоски (плавный переход цвета от серого к белому)
        int currentProgressWidth = (int) (barWidth * progress);
        context.fill(x, y, x + currentProgressWidth, y + barHeight, 0xFFFFFFFF);

        // Текст
        String text = "Идёт осмотр...";
        context.drawText(client.textRenderer, text, w / 2 - client.textRenderer.getWidth(text) / 2, y - 12, 0xFFFFFF, true);
    }
}