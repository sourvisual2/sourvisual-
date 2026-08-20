package com.sourvisual.mixin;

import com.sourvisual.config.SourVisualConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Shadow
    public Entity targetedEntity;

    // Включает встроенный игровой эффект "обводки" (тот же, что у Glowing)
    // для сущности, на которую наведён прицел, если Target ESP включён.
    @Inject(method = "hasOutline", at = @At("HEAD"), cancellable = true)
    private void sourvisual$targetEsp(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (SourVisualConfig.targetEspEnabled && entity != null && entity == this.targetedEntity) {
            cir.setReturnValue(true);
        }
    }
}
