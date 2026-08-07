package com.sourvisual.mixin;

import com.sourvisual.client.config.ModConfig;
import net.minecraft.client.texture.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(targets = "net.minecraft.client.render.OverlayTexture")
public class HitColorMixin {

    @Inject(method = "upload", at = @At("HEAD"))
    private void modifyHitColor(CallbackInfo ci) {
        if (!ModConfig.hitColorEnabled) return;

        try {
            // Получаем поле image через рефлексию
            NativeImage image = null;
            for (Field f : this.getClass().getSuperclass().getDeclaredFields()) {
                f.setAccessible(true);
                Object val = f.get(this);
                if (val instanceof NativeImage) {
                    image = (NativeImage) val;
                    break;
                }
            }

            // Если не нашли через суперкласс — ищем в самом классе
            if (image == null) {
                for (Field f : this.getClass().getDeclaredFields()) {
                    f.setAccessible(true);
                    Object val = f.get(this);
                    if (val instanceof NativeImage) {
                        image = (NativeImage) val;
                        break;
                    }
                }
            }

            if (image == null) return;

            int r = ModConfig.hitColorR;
            int g = ModConfig.hitColorG;
            int b = ModConfig.hitColorB;

            // NativeImage хранит в ABGR формате
            int abgr = (0xFF << 24) | (b << 16) | (g << 8) | r;

            // Верхняя строка (y=0) — цвет удара
            for (int u = 0; u < 16; u++) {
                image.setColor(u, 0, abgr);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
