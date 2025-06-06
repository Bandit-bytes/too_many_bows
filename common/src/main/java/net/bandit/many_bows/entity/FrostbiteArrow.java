package net.bandit.many_bows.entity;

import net.bandit.many_bows.registry.EntityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FrostbiteArrow extends AbstractArrow {

    private boolean hasHit = false;
    private int hitTimer = 0;
    private final int maxHitDuration = 40;

    public FrostbiteArrow(EntityType<? extends FrostbiteArrow> entityType, Level level) {
        super(entityType, level);
    }

    public FrostbiteArrow(Level level, LivingEntity shooter) {
        super(EntityRegistry.FROSTBITE_ARROW.get(), shooter, level);
    }

    @Override
    public void tick() {
        super.tick();

        if (hasHit) {
            hitTimer++;
            if (hitTimer >= maxHitDuration) {
                this.discard();
                return;
            }
        }

        if (this.level().isClientSide()) {
            double speedFactor = 0.1D;
            Vec3 motion = this.getDeltaMovement();
            for (int i = 0; i < 5; i++) {
                double xOffset = (this.random.nextDouble() - 0.5D) * 0.3D;
                double yOffset = (this.random.nextDouble() - 0.5D) * 0.3D;
                double zOffset = (this.random.nextDouble() - 0.5D) * 0.3D;

                this.level().addParticle(ParticleTypes.SNOWFLAKE,
                        this.getX() + motion.x * i * speedFactor,
                        this.getY() + motion.y * i * speedFactor,
                        this.getZ() + motion.z * i * speedFactor,
                        xOffset, yOffset, zOffset);

                this.level().addParticle(ParticleTypes.CLOUD,
                        this.getX(), this.getY(), this.getZ(),
                        0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide() && result.getEntity() instanceof LivingEntity hitEntity) {
            hitEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 75, 5));

            createFrostExplosion(result.getLocation(), hitEntity);
            this.hasHit = true;
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!this.level().isClientSide()) {
            createFrostExplosion(result.getLocation(), null);
            this.hasHit = true;
        }
    }

    private void createFrostExplosion(Vec3 position, @Nullable LivingEntity entityHit) {
        int radius = 4;
        List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius));
        for (LivingEntity entity : entities) {
            if (entity != this.getOwner() && entity != entityHit) {
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 75, 5)); // Apply slowness
            }
        }

        for (int i = 0; i < 100; i++) {
            double xOffset = (random.nextDouble() - 0.5D) * 2.0D;
            double yOffset = random.nextDouble();
            double zOffset = (random.nextDouble() - 0.5D) * 2.0D;
            this.level().addParticle(ParticleTypes.SNOWFLAKE,
                    position.x + xOffset,
                    position.y + yOffset,
                    position.z + zOffset,
                    0, 0.1D, 0);
        }

        for (int i = 0; i < 30; i++) {
            double xOffset = (random.nextDouble() - 0.5D) * 2.0D;
            double yOffset = random.nextDouble() * 0.5D;
            double zOffset = (random.nextDouble() - 0.5D) * 2.0D;
            this.level().addParticle(ParticleTypes.CLOUD,
                    position.x + xOffset,
                    position.y + yOffset,
                    position.z + zOffset,
                    0, 0, 0);
        }

        this.level().playSound(null, position.x, position.y, position.z,
                SoundEvents.GLASS_BREAK, this.getSoundSource(), 1.0F, 0.8F);
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}

