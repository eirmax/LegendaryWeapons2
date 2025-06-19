package com.eirmax.mixin;

import com.eirmax.components.PlayerEntityMixinAccessor;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements PlayerEntityMixinAccessor {
    @Unique
    private static final TrackedData<Boolean> IGNORE_FALL_DAMAGE =
            DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void injectInitDataTracker(CallbackInfo ci) {
        ((PlayerEntity)(Object)this).getDataTracker().startTracking(IGNORE_FALL_DAMAGE, false);
    }

    @Unique
    public void fabric$setIgnoreFallDamage(boolean value) {
        ((PlayerEntity)(Object)this).getDataTracker().set(IGNORE_FALL_DAMAGE, value);
    }

    @Unique
    public boolean fabric$getIgnoreFallDamage() {
        return ((PlayerEntity)(Object)this).getDataTracker().get(IGNORE_FALL_DAMAGE);
    }

    @Inject(method = "handleFallDamage", at = @At("HEAD"), cancellable = true)
    private void ignoreFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (fabric$getIgnoreFallDamage()) {
            cir.setReturnValue(false);
            fabric$setIgnoreFallDamage(false);
        }
    }
}