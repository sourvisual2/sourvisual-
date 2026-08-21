package com.sourvisual.render;

import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class TargetMarkerRenderer {

    public static void render(WorldRenderContext context) {
        if (!SourVisualConfig.targetEspEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        Entity target = client.targetedEntity;
        if (target == null || !target.isAlive()) return;

        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();
        if (matrices == null || consumers == null || context.camera() == null) return;

        Vec3d cam = context.camera().getPos();

        int color = SourVisualConfig.getAccentColor();
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        VertexConsumer lineConsumer = consumers.getBuffer(RenderLayer.getLines());

        Box box = target.getBoundingBox();
        double cx = (box.minX + box.maxX) / 2.0;
        double cz = (box.minZ + box.maxZ) / 2.0;
        double cy = box.maxY + 0.35;

        double half = 0.28;
        double thin = 0.05;
        Box marker = new Box(cx - half, cy - thin, cz - half, cx + half, cy + thin, cz + half);

        float angle = (System.currentTimeMillis() % 3600L) / 8f;

        matrices.push();
        matrices.translate(-cam.x, -cam.y, -cam.z);
        matrices.translate(cx, cy, cz);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle));
        matrices.translate(-cx, -cy, -cz);

        RenderSystem.disableDepthTest();
        WorldRenderer.drawBox(matrices, lineConsumer, marker, r, g, b, 1.0f);
        RenderSystem.enableDepthTest();

        matrices.pop();
    }

    private TargetMarkerRenderer() {}
}
