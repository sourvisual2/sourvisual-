package com.sourvisual.mixin;

import com.sourvisual.config.SourVisualConfig;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {

    @Shadow
    @Final
    private NativeImage image;

    @Shadow
    @Final
    private NativeImageBackedTexture texture;

    // После того как игра посчитает обычную карту освещения (16x16),
    // если Fullbright включён — принудительно заливаем её чисто белым
    // и перезагружаем текстуру. Это даёт настоящую полную яркость
    // независимо от игровой гаммы.
    @Inject(method = "update", at = @At("TAIL"))
    private void sourvisual$applyFullbright(float delta, CallbackInfo ci) {
        if (!SourVisualConfig.fullbrightEnabled) {
            return;
        }
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                image.setColor(x, y, 0xFFFFFFFF);
            }
        }
        texture.upload();
    }
}
