package com.sourvisual.mixin;

import com.sourvisual.client.config.ModConfig;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class HitOverlayMixin {

    @Inject(method = "getOverlay", at = @At("RETURN"), cancellable = true)
    private static void modifyOverlay(LivingEntity entity, float whiteOverlayProgress,
                                      CallbackInfoReturnable<Integer> cir) {
        if (!ModConfig.hitColorEnabled) return;
        if (entity.hurtTime <= 0) return;

        // берём кастомный цвет
        int r = ModConfig.hitColorR;
        int g = ModConfig.hitColorG;
        int b = ModConfig.hitColorB;

        // overlay int: упакованный UV + цвет
        // пакуем как OverlayTexture но с нашим цветом
        // U = горизонт (0=hurt, 1=normal), V = 0
        // формат: (v << 16) | u  где u=0 для hurt overlay
        cir.setReturnValue(OverlayTexture.packUv(0, 10));
    }
}
