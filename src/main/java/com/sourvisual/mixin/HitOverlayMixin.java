package com.sourvisual.mixin;

import com.sourvisual.client.config.ModConfig;
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

        int r = ModConfig.hitColorR;
        int g = ModConfig.hitColorG;
        int b = ModConfig.hitColorB;

        // UV упакован: нижние 16 бит = U (0 = hurt), верхние 16 = V
        // Просто возвращаем стандартный hurt overlay
        // Реальный цвет меняем через shader overlay texture
        // В 1.21 overlay = packed (v << 16 | u), u=0 = red hurt
        cir.setReturnValue(0x00FF0000);
    }
}
