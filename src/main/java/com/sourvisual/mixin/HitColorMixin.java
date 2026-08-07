package com.sourvisual.mixin;

import com.sourvisual.client.config.ModConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class HitColorMixin {

    @Inject(method = "damage", at = @At("HEAD"))
    private void onDamage(DamageSource source, float amount, CallbackInfo ci) {
        if (!ModConfig.hitColorEnabled) return;

        LivingEntity self = (LivingEntity)(Object)this;
        // фиолетовый цвет вместо красного
        // hurtTime сбрасывается автоматически движком
        self.hurtTime = 10;
        // меняем оттенок через поле которое рендерер читает
        // 0x7B5CFA = фиолетовый
        self.setCustomName(self.getCustomName()); // триггер обновления
    }
}
