package com.sourvisual.mixin;

import com.sourvisual.client.config.ModConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class HitColorMixin {

    @Inject(method = "damage", at = @At("RETURN"))
    private void onDamage(DamageSource source, float amount,
                          CallbackInfoReturnable<Boolean> cir) {
        if (!ModConfig.hitColorEnabled) return;
        if (!Boolean.TRUE.equals(cir.getReturnValue())) return;

        LivingEntity self = (LivingEntity)(Object)this;
        self.hurtTime    = 10;
        self.maxHurtTime = 10;
    }
}
