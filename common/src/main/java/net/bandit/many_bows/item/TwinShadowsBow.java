package net.bandit.many_bows.item;

import net.bandit.many_bows.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class TwinShadowsBow extends BowItem {

    public TwinShadowsBow(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack bowStack, Level level, LivingEntity entity, int chargeTime) {
        if (entity instanceof Player player) {
            List<ItemStack> projectiles = draw(bowStack, player.getProjectile(bowStack), player);

            if (!projectiles.isEmpty() || player.getAbilities().instabuild) {
                int charge = this.getUseDuration(bowStack, entity) - chargeTime;
                float power = getPowerForTime(charge);

                if (power >= 0.1F) {
                    if (level instanceof ServerLevel serverLevel) {
                        fireTwinArrows(serverLevel, player, bowStack, projectiles, power);
                    }

                    // Play shooting sound & apply durability loss
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
                    player.awardStat(Stats.ITEM_USED.get(this));

                    if (!player.getAbilities().instabuild) {
                        bowStack.hurtAndBreak(1, player, (EquipmentSlot.MAINHAND));
                    }
                }
            }
        }
    }

    private void fireTwinArrows(ServerLevel serverLevel, Player player, ItemStack bowStack, List<ItemStack> projectileStacks, float power) {
        if (projectileStacks.isEmpty()) return;

        ItemStack projectileStack = projectileStacks.get(0); // Get the first available projectile
        ArrowItem arrowItem = (ArrowItem) (projectileStack.getItem() instanceof ArrowItem ? projectileStack.getItem() : Items.ARROW);

        // Fire the first (Light) arrow
        AbstractArrow lightArrow = arrowItem.createArrow(serverLevel, projectileStack, player, bowStack);
        lightArrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 2.5F, 1.0F);
        lightArrow.setBaseDamage(6.0);
        lightArrow.addTag("light");
        lightArrow.setCustomName(Component.literal(ChatFormatting.WHITE + "Light Arrow"));
        lightArrow.pickup = AbstractArrow.Pickup.ALLOWED;
        serverLevel.addFreshEntity(lightArrow);

        // Fire the second (Dark) arrow
        AbstractArrow darkArrow = arrowItem.createArrow(serverLevel, projectileStack, player, bowStack);
        darkArrow.shootFromRotation(player, player.getXRot(), player.getYRot() + 5.0F, 0.0F, power * 2.5F, 1.0F);
        darkArrow.setBaseDamage(8.0);
        darkArrow.addTag("dark");
        darkArrow.setCustomName(Component.literal(ChatFormatting.DARK_GRAY + "Dark Arrow"));
        darkArrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        serverLevel.addFreshEntity(darkArrow);

        // Consume only 1 arrow from inventory
        if (!player.getAbilities().instabuild) {
            projectileStack.shrink(1);
            if (projectileStack.isEmpty()) {
                projectileStacks.remove(0);
            }
        }
    }

    public static float getPowerForTime(int pCharge) {
        float f = (float) pCharge / 16.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        return Math.min(f, 1.0F);
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return ARROW_ONLY;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 16;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bowStack = player.getItemInHand(hand);
        boolean hasArrows = !player.getProjectile(bowStack).isEmpty();
        if (!hasArrows) {
            return InteractionResultHolder.fail(bowStack);
        } else {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(bowStack);
        }
    }

    @Override
    public int getDefaultProjectileRange() {
        return 64;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ItemRegistry.POWER_CRYSTAL.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (Screen.hasShiftDown()) {
            tooltipComponents.add(Component.translatable("item.many_bows.twin_shadows.tooltip").withStyle(ChatFormatting.AQUA));
            tooltipComponents.add(Component.translatable("item.many_bows.twin_shadows.tooltip.ability").withStyle(ChatFormatting.GOLD));
            tooltipComponents.add(Component.translatable("item.many_bows.twin_shadows.tooltip.legend").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        } else {
            tooltipComponents.add(Component.translatable("item.too_many_bows.hold_shift"));
        }
    }
}
