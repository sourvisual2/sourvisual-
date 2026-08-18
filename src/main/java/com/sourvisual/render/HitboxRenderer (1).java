package com.sourvisual.render;

import com.sourvisual.config.SourVisualConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class HitboxRenderer {

    private static final int FILL_SLICES = 16;

    public static void render(WorldRenderContext context) {
        if (!SourVisualConfig.hitboxEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();
        if (matrices == null || consumers == null || context.camera() == null) return;

        Vec3d cam = context.camera().getPos();

        int color = SourVisualConfig.getHitboxColor();
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        VertexConsumer lineConsumer = consumers.getBuffer(RenderLayer.getLines());

        for (Entity entity : client.world.getEntities()) {
            if (!entity.isAlive()) continue;

            Box box = entity.getBoundingBox();

            matrices.push();
            matrices.translate(-cam.x, -cam.y, -cam.z);

            WorldRenderer.drawBox(matrices, lineConsumer, box, r, g, b, 1.0f);

            if (SourVisualConfig.hitboxFilled) {
                drawFilledSlices(matrices, lineConsumer, box, r, g, b);
            }

            matrices.pop();
        }
    }

    // "Заливка" через плотную стопку контурных срезов по высоте —
    // использует тот же самый проверенный метод WorldRenderer.drawBox,
    // без сырых вершин, чтобы не рисковать крашем на слабых GPU.
    private static void drawFilledSlices(MatrixStack matrices, VertexConsumer consumer, Box box,
                                          float r, float g, float b) {
        double height = box.maxY - box.minY;
        for (int i = 1; i < FILL_SLICES; i++) {
            double t = (double) i / FILL_SLICES;
            double y = box.minY + height * t;
            Box slice = new Box(box.minX, y, box.minZ, box.maxX, y, box.maxZ);
            WorldRenderer.drawBox(matrices, consumer, slice, r, g, b, 1.0f);
        }
    }

    private HitboxRenderer() {}
}
