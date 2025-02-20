package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.HunterArrow;
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

public class HunterBow extends BowItem {

    public HunterBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge);
            if (power >= 0.1F) {
                ItemStack arrowStack = player.getProjectile(stack);
                boolean isCreative = player.getAbilities().instabuild;
                boolean hasInfinity = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
                boolean hasArrows = !arrowStack.isEmpty() || isCreative || hasInfinity;

                if (hasArrows) {
                    if (arrowStack.isEmpty() && hasInfinity) {
                        arrowStack = new ItemStack(Items.ARROW);
                    }

                    HunterArrow hunterArrow = new HunterArrow(level, player);
                    hunterArrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);
                    applyEnchantments(stack, hunterArrow);

                    if (hasInfinity) {
                        hunterArrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                    }

                    if (!isCreative && !hasInfinity) {
                        arrowStack.shrink(1);
                        if (arrowStack.isEmpty()) {
                            player.getInventory().removeItem(arrowStack);
                        }
                    }

                    level.addFreshEntity(hunterArrow);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);

                    stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                } else {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }


    private void applyEnchantments(ItemStack stack, HunterArrow hunterArrow) {
        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
        if (powerLevel > 0) {
            hunterArrow.setBaseDamage(hunterArrow.getBaseDamage() + (powerLevel * 0.5) + 1.0);
        }

        int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
        if (punchLevel > 0) {
            hunterArrow.setKnockback(punchLevel);
        }

        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
            hunterArrow.setSecondsOnFire(100);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        // Default tooltip that always shows
        tooltip.add(Component.translatable("item.too_many_bows.hunter_bow_bow").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.too_many_bows.hunter_bow.tooltip").withStyle(ChatFormatting.GREEN));

        // Check if the player is holding the Shift key
        if (Screen.hasShiftDown()) {
            // Display list of passive mobs the bow is effective against
            tooltip.add(Component.translatable("item.too_many_bows.hunter_bow.shift_title").withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.translatable("item.too_many_bows.hunter_bow.mob1").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("item.too_many_bows.hunter_bow.mob2").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("item.too_many_bows.hunter_bow.mob3").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("item.too_many_bows.hunter_bow.mob4").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("item.too_many_bows.hunter_bow.mob5").withStyle(ChatFormatting.GREEN));
        } else {
            // Prompt to hold Shift for more information
            tooltip.add(Component.translatable("item.too_many_bows.hunter_bow.hold_shift").withStyle(ChatFormatting.GRAY));
        }
    }



    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ItemRegistry.POWER_CRYSTAL.get());
    }
}
