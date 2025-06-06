package net.bandit.many_bows.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class LightOrbEntity extends Entity {

    private static final double FOLLOW_DISTANCE = 1.5D;
    private BlockPos lastLightPos = null; // Track the last position where we placed light

    public LightOrbEntity(EntityType<? extends LightOrbEntity> type, Level world) {
        super(type, world);
        this.noPhysics = true;
        this.setInvisible(true);
        this.setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return;

        Player nearestPlayer = this.level().getNearestPlayer(this, 5.0D);

        if (nearestPlayer != null && isPlayerHoldingTorchBow(nearestPlayer)) {
            double dx = nearestPlayer.getX() - this.getX();
            double dy = (nearestPlayer.getY() + 1.5D) - this.getY();
            double dz = nearestPlayer.getZ() - this.getZ();

            // Smoothly follow player
            this.move(MoverType.SELF, this.getDeltaMovement().add(dx * 0.2D, dy * 0.2D, dz * 0.2D));

            BlockPos currentPos = this.blockPosition();

            // If we've moved to a new block position
            if (lastLightPos == null || !lastLightPos.equals(currentPos)) {
                // Remove old light block if it was placed
                if (lastLightPos != null && level().getBlockState(lastLightPos).is(Blocks.LIGHT)) {
                    level().setBlockAndUpdate(lastLightPos, Blocks.AIR.defaultBlockState());
                }

                // Place new light block if air
                BlockState currentState = level().getBlockState(currentPos);
                if (currentState.isAir()) {
                    level().setBlockAndUpdate(currentPos, Blocks.LIGHT.defaultBlockState());
                    lastLightPos = currentPos;
                }
            }
        } else {
            // No nearby player or not holding bow anymore — cleanup
            if (lastLightPos != null && level().getBlockState(lastLightPos).is(Blocks.LIGHT)) {
                level().setBlockAndUpdate(lastLightPos, Blocks.AIR.defaultBlockState());
            }
            this.discard();
        }
    }

    private boolean isPlayerHoldingTorchBow(Player player) {
        return player.getMainHandItem().getItem() instanceof net.bandit.many_bows.item.TorchbearerBow
                || player.getOffhandItem().getItem() instanceof net.bandit.many_bows.item.TorchbearerBow;
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
