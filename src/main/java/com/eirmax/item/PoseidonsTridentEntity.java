package com.eirmax.item;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class PoseidonsTridentEntity extends TridentEntity {


    private static final TrackedData<Byte> LOYALTY = DataTracker.registerData(PoseidonsTridentEntity.class, TrackedDataHandlerRegistry.BYTE);
    private boolean customDealtDamage = false;


    public PoseidonsTridentEntity(EntityType<? extends TridentEntity> entityType, World world) {
        super(entityType, world);
        this.dataTracker.set(LOYALTY, (byte)3);
    }
    public PoseidonsTridentEntity(World world, LivingEntity owner, ItemStack stack) {
        super(world, owner, stack);
        this.dataTracker.set(LOYALTY, (byte)3);
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        super.onEntityHit(hit);
        this.customDealtDamage = true;

        Entity entity = hit.getEntity();
        Entity owner = this.getOwner();
        World world = this.getWorld();

        float damage = 9.0F;
        if (entity instanceof LivingEntity living) {
            damage += EnchantmentHelper.getAttackDamage(this.asItemStack(), living.getGroup());
        }
        DamageSource source = world.getDamageSources().trident(this, owner != null ? owner : this);
        entity.damage(source, damage);

        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            Random random = serverWorld.getRandom();
            int count = random.nextFloat() < 0.5f ? 3 : 1;
            for (int i = 0; i < count; i++) {
                LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(serverWorld);
                if (lightning != null) {
                    lightning.refreshPositionAfterTeleport(entity.getX(), entity.getY(), entity.getZ());
                    serverWorld.spawnEntity(lightning);
                }
            }
            world.playSound(null, entity.getBlockPos(), SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT,  SoundCategory.WEATHER, 5.0F, 1.0F);
        }
        this.setVelocity(this.getVelocity().multiply(-0.01, -0.1, -0.01));
        this.playSound(SoundEvents.ITEM_TRIDENT_HIT, 1.0F, 1.0F);
    }


    private boolean isOwnerAlive() {
        Entity entity = this.getOwner();
        if (entity != null && entity.isAlive()) {
            return !(entity instanceof ServerPlayerEntity) || !entity.isSpectator();
        } else {
            return false;
        }
    }


    @Override
    public void tick() {
        if (this.inGroundTime > 4) {
            this.customDealtDamage = true;
            Entity owner = this.getOwner();
            int loyalty = 3;
            if ((this.customDealtDamage || this.isNoClip()) && owner != null) {
                if (!this.isOwnerAlive()) {
                    if (!this.getWorld().isClient() && this.pickupType == PickupPermission.ALLOWED) {
                        this.dropStack(this.asItemStack(), 0.1F);
                    }
                    this.discard();
                } else {
                    this.setNoClip(true);
                    Vec3d vec3d = owner.getEyePos().subtract(this.getPos());
                    this.setPos(this.getX(), this.getY() + vec3d.y * 0.015 * (double) loyalty, this.getZ());
                    if (this.getWorld().isClient()) {
                        this.lastRenderY = this.getY();
                    }
                    double d = 0.05 * (double) loyalty;
                    this.setVelocity(this.getVelocity().multiply(0.95).add(vec3d.normalize().multiply(d)));
                    if (this.returnTimer == 0) {
                        this.playSound(SoundEvents.ITEM_TRIDENT_RETURN, 10.0F, 1.0F);
                    }
                    ++this.returnTimer;
                }
            }
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putByte("Loyalty", (byte)3);
    }
    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(LOYALTY, (byte)3);
    }
}

