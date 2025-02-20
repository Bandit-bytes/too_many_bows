package net.bandit.many_bows.item;

import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ScatterBow extends BowItem {

    private static final int NUM_ARROWS = 8;

    public ScatterBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge);

            // Check for Infinity enchantment or Creative mode
            boolean hasInfinity = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
            int arrowsToShoot = hasInfinity ? NUM_ARROWS : Math.min(NUM_ARROWS, countArrows(player));

            if (power >= 0.1F && (hasInfinity || consumeArrow(player, arrowsToShoot))) {
                for (int i = 0; i < arrowsToShoot; i++) {
                    Arrow arrow = new Arrow(level, player);
                    arrow.setBaseDamage(1.0);

                    // Apply enchantment
                    int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
                    if (powerLevel > 0) {
                        arrow.setBaseDamage(arrow.getBaseDamage() + powerLevel * 0.5 + 0.5);
                    }

                    int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
                    if (punchLevel > 0) {
                        arrow.setKnockback(punchLevel);
                    }

                    if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
                        arrow.setSecondsOnFire(100);
                    }

                    // Prevent pickup if Infinity is enabled
                    if (hasInfinity) {
                        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
                    }

                    // Slightly randomize the shooting direction
                    float yawOffset = (level.getRandom().nextFloat() - 0.5F) * 20F;
                    float pitchOffset = (level.getRandom().nextFloat() - 0.5F) * 10F;
                    arrow.shootFromRotation(player, player.getXRot() + pitchOffset, player.getYRot() + yawOffset, 0.0F, power * 3.0F, 1.0F);

                    level.addFreshEntity(arrow);
                }

                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
                stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
            } else {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
        }
    }
    private boolean consumeArrow(Player player, int count) {
        if (player.getAbilities().instabuild) {
            return true; // Creative mode bypass
        }

        int arrowsRemoved = 0;

        for (ItemStack stack : player.getInventory().items) {
            ItemStack projectile = player.getProjectile(stack);
            if (!projectile.isEmpty() && arrowsRemoved < count) {
                int removeAmount = Math.min(projectile.getCount(), count - arrowsRemoved);
                projectile.shrink(removeAmount);
                arrowsRemoved += removeAmount;
            }
            if (arrowsRemoved >= count) {
                return true;
            }
        }
        return false;
    }

    private int countArrows(Player player) {
        int arrowCount = 0;
        for (ItemStack stack : player.getInventory().items) {
            ItemStack projectile = player.getProjectile(stack);
            if (!projectile.isEmpty()) {
                arrowCount += projectile.getCount();
            }
        }
        return arrowCount;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
        tooltip.add(Component.translatable("item.many_bows.scatter_bow.tooltip").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.many_bows.scatter_bow.tooltip.ability").withStyle(ChatFormatting.DARK_GREEN));
    }else {
            tooltip.add(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ItemRegistry.POWER_CRYSTAL.get());
    }
}
