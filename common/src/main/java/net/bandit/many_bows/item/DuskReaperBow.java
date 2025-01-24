package net.bandit.many_bows.item;

import net.bandit.many_bows.entity.DuskReaperArrow;
import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class DuskReaperBow extends BowItem {

    public DuskReaperBow(Properties properties) {
        super(properties);
    }
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (entity instanceof Player player && !level.isClientSide()) {
            int charge = this.getUseDuration(stack) - timeCharged;
            float power = getPowerForTime(charge);

            if (power >= 0.1F && consumeSoulFragments(player)) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.3F, 0.5F);

                ItemStack arrowStack = player.getProjectile(stack);
                DuskReaperArrow arrow;

                if (!arrowStack.isEmpty() && arrowStack.getItem() instanceof ArrowItem arrowItem) {
                    arrow = new DuskReaperArrow(level, player);
                    arrow.setBaseDamage(2.0);
                } else {
                    arrow = new DuskReaperArrow(level, player);
                }
                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 1.0F);
                arrow.setCritArrow(charge >= 20);
                applyEnchantments(stack, arrow);
                level.addFreshEntity(arrow);

                if (!player.getAbilities().instabuild) {
                    arrowStack.shrink(1);
                    if (arrowStack.isEmpty()) {
                        player.getInventory().removeItem(arrowStack);
                    }
                }

                stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                player.awardStat(Stats.ITEM_USED.get(this));
            } else {
                player.displayClientMessage(Component.translatable("item.many_bows.dusk_reaper.no_soul_fragments").withStyle(ChatFormatting.RED), true);
            }
        }
    }

    private boolean consumeSoulFragments(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == ItemRegistry.SOUL_FRAGMENT.get()) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private ItemStack findArrowInInventory(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof ArrowItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private void applyEnchantments(ItemStack stack, DuskReaperArrow arrow) {
        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
        if (powerLevel > 0) {
            arrow.setBaseDamage(arrow.getBaseDamage() + (powerLevel * 0.5) + 1.5);
        }

        int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
        if (punchLevel > 0) {
            arrow.setKnockback(punchLevel);
        }

        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
            arrow.setSecondsOnFire(100);
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

    @Override
    public @NotNull Predicate<ItemStack> getAllSupportedProjectiles() {
        return stack -> true; // Allow any type of projectile, though soul fragments are prioritized.
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.many_bows.dusk_reaper.tooltip.info").withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.add(Component.translatable("item.many_bows.dusk_reaper.tooltip.soul_fragments").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.many_bows.dusk_reaper.tooltip.mark").withStyle(ChatFormatting.RED));
            tooltip.add(Component.translatable("item.many_bows.dusk_reaper.tooltip.spectral").withStyle(ChatFormatting.DARK_AQUA));
            tooltip.add(Component.translatable("item.many_bows.dusk_reaper.tooltip.effect").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("item.many_bows.dusk_reaper.tooltip.debuff").withStyle(ChatFormatting.DARK_RED));
            tooltip.add(Component.translatable("item.many_bows.dusk_reaper.tooltip.glow").withStyle(ChatFormatting.LIGHT_PURPLE));
        } else {
            tooltip.add(Component.translatable("item.too_many_bows.hold_shift").withStyle(ChatFormatting.YELLOW));
        }
    }
}
