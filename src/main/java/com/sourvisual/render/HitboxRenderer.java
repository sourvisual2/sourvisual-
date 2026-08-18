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
import org.joml.Matrix4f;

public class HitboxRenderer {

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
        float fillAlpha = SourVisualConfig.hitboxOpacity / 100f;

        VertexConsumer lineConsumer = consumers.getBuffer(RenderLayer.getLines());
        VertexConsumer fillConsumer = SourVisualConfig.hitboxFilled
                ? consumers.getBuffer(RenderLayer.getDebugFilledBox())
                : null;

        for (Entity entity : client.world.getEntities()) {
            if (entity == client.cameraEntity) continue;
            if (!entity.isAlive()) continue;

            Box box = entity.getBoundingBox();

            matrices.push();
            matrices.translate(-cam.x, -cam.y, -cam.z);

            WorldRenderer.drawBox(matrices, lineConsumer, box, r, g, b, 1.0f);

            if (fillConsumer != null) {
                drawFilledBox(matrices, fillConsumer, box, r, g, b, fillAlpha);
            }

            matrices.pop();
        }
    }

    private static void drawFilledBox(MatrixStack matrices, VertexConsumer consumer, Box box,
                                       float r, float g, float b, float a) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float x1 = (float) box.minX, y1 = (float) box.minY, z1 = (float) box.minZ;
        float x2 = (float) box.maxX, y2 = (float) box.maxY, z2 = (float) box.maxZ;

        quad(consumer, matrix, x1, y1, z1, x1, y2, z1, x1, y2, z2, x1, y1, z2, r, g, b, a);
        quad(consumer, matrix, x2, y1, z2, x2, y2, z2, x2, y2, z1, x2, y1, z1, r, g, b, a);
        quad(consumer, matrix, x1, y1, z1, x1, y1, z2, x2, y1, z2, x2, y1, z1, r, g, b, a);
        quad(consumer, matrix, x1, y2, z2, x1, y2, z1, x2, y2, z1, x2, y2, z2, r, g, b, a);
        quad(consumer, matrix, x2, y1, z1, x2, y2, z1, x1, y2, z1, x1, y1, z1, r, g, b, a);
        quad(consumer, matrix, x1, y1, z2, x1, y2, z2, x2, y2, z2, x2, y1, z2, r, g, b, a);
    }

    private static void quad(VertexConsumer consumer, Matrix4f matrix,
                              float x1, float y1, float z1,
                              float x2, float y2, float z2,
                              float x3, float y3, float z3,
                              float x4, float y4, float z4,
                              float r, float g, float b, float a) {
        consumer.vertex(matrix, x1, y1, z1).color(r, g, b, a);
        consumer.vertex(matrix, x2, y2, z2).color(r, g, b, a);
        consumer.vertex(matrix, x3, y3, z3).color(r, g, b, a);
        consumer.vertex(matrix, x4, y4, z4).color(r, g, b, a);
    }

    private HitboxRenderer() {}
}
          }
