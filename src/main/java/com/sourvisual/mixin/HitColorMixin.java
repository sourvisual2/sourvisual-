package com.sourvisual.mixin;

import com.sourvisual.client.config.ModConfig;
import net.minecraft.client.texture.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.render.OverlayTexture")
public class HitColorMixin {

    // method_23207 = reloadOverlay в intermediary для 1.21
    @Inject(method = "method_23207", at = @At("TAIL"))
    private void modifyHitColor(CallbackInfo ci) {
        if (!ModConfig.hitColorEnabled) return;

        try {
            NativeImage image = null;

            for (java.lang.reflect.Field f : this.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                Object val = f.get(this);
                if (val instanceof NativeImage ni) {
                    image = ni;
                    break;
                }
            }

            if (image == null) return;

            int r = ModConfig.hitColorR;
            int g = ModConfig.hitColorG;
            int b = ModConfig.hitColorB;

            // NativeImage хранит пиксели в ABGR
            int abgr = (0xFF << 24) | (b << 16) | (g << 8) | r;

            // y=0 — hurt overlay строка
            for (int u = 0; u < 16; u++) {
                image.setColor(u, 0, abgr);
            }

        } catch (Exception ignored) {}
    }
}
