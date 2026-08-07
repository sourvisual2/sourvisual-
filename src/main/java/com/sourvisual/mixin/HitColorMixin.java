package com.sourvisual.mixin;

import com.sourvisual.client.config.ModConfig;
import net.minecraft.client.texture.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Используем строковое имя класса — избегаем проблем с маппингами
@Mixin(targets = "net.minecraft.client.render.OverlayTexture")
public class HitColorMixin {

    @Shadow
    private NativeImage image;

    @Inject(method = "upload", at = @At("HEAD"))
    private void modifyHitColor(CallbackInfo ci) {
        if (!ModConfig.hitColorEnabled) return;
        if (image == null) return;

        int r = ModConfig.hitColorR;
        int g = ModConfig.hitColorG;
        int b = ModConfig.hitColorB;

        // Верхняя строка пикселей (v=0) — цвет удара
        // NativeImage хранит в формате ABGR
        int abgr = (0xFF << 24) | (b << 16) | (g << 8) | r;
        for (int u = 0; u < 16; u++) {
            image.setColor(u, 0, abgr);
        }
    }
}
