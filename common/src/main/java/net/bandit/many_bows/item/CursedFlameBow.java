package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.CursedFlameArrow;
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
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CursedFlameBow extends BowItem {

    public CursedFlameBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge);

            if (power >= 0.1F) {
                boolean hasInfinity = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
                ItemStack arrowStack = hasInfinity ? ItemStack.EMPTY : player.getProjectile(stack);

                if (hasInfinity || !arrowStack.isEmpty()) {
                    CursedFlameArrow cursedFlameArrow = new CursedFlameArrow(level, player);
                    cursedFlameArrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);

                    // Apply enchantment effects
                    int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
                    if (powerLevel > 0) {
                        cursedFlameArrow.setBaseDamage(cursedFlameArrow.getBaseDamage() + (powerLevel * 0.5) + 1.5);
                    }

                    int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
                    if (punchLevel > 0) {
                        cursedFlameArrow.setKnockback(punchLevel);
                    }

                    if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
                        cursedFlameArrow.setSecondsOnFire(100);
                    }

                    if (hasInfinity) {
                        cursedFlameArrow.pickup = AbstractArrow.Pickup.DISALLOWED;
                    }

                    level.addFreshEntity(cursedFlameArrow);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SKELETON_AMBIENT, SoundSource.PLAYERS, 1.0F, 1.2F);

                    // Consume an arrow if not in Creative mode or with Infinity
                    if (!hasInfinity && !arrowStack.isEmpty()) {
                        arrowStack.shrink(1);
                        if (arrowStack.isEmpty()) {
                            player.getInventory().removeItem(arrowStack);
                        }
                    }
                    stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                } else {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
        tooltip.add(Component.translatable("item.too_many_bows.cursed_flame_bow.tooltip").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("item.too_many_bows.cursed_flame_bow.tooltip.ability").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.too_many_bows.cursed_flame_bow.tooltip.legend").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
    }
    else {
        tooltip.add(Component.translatable("item.too_many_bows.hold_shift"));
    }
}
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ItemRegistry.POWER_CRYSTAL.get());
    }
}
