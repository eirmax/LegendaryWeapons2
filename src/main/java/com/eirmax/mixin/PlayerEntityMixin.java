package com.eirmax.mixin;

import com.eirmax.item.custom.DragonKatana;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void preventFallDamage(DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity)(Object)this;

        if (damageSource.isOf(DamageTypes.FALL) &&
                DragonKatana.shouldNegateFallDamage(player)) {
            cir.setReturnValue(false);
        }
    }
}