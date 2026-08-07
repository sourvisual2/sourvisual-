package com.sourvisual.mixin;

import com.sourvisual.client.config.ModConfig;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.OverlayTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OverlayTexture.class)
public class HitColorMixin {

    @Shadow
    private NativeImage image;

    @Inject(method = "close", at = @At("HEAD"))
    private void dummy(CallbackInfo ci) {}

    // Инжектимся в upload() — метод который загружает текстуру на GPU
    @Inject(method = "upload", at = @At("HEAD"))
    private void modifyHitColor(CallbackInfo ci) {
        if (!ModConfig.hitColorEnabled) return;
        if (image == null) return;

        int r = ModConfig.hitColorR;
        int g = ModConfig.hitColorG;
        int b = ModConfig.hitColorB;

        // OverlayTexture — 16x2 пикселей
        // Верхняя строка (v=0) — цвет удара (hurt overlay)
        // Нижняя строка (v=1) — белый flash
        // Пиксели хранятся в ABGR формате в NativeImage
        for (int u = 0; u < 16; u++) {
            // строка 0 = hurt color, непрозрачный
            // NativeImage.getColor возвращает ABGR
            // Ставим наш цвет в строку hurt (v=0)
            int abgr = (0xFF << 24) | (b << 16) | (g << 8) | r;
            image.setColor(u, 0, abgr);
        }
    }
}
