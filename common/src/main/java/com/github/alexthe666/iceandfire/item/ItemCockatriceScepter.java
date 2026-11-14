package com.github.alexthe666.iceandfire.item;

import net.minecraft.core.particles.ColorParticleOption;
import com.github.alexthe666.iceandfire.entity.EntityGorgon;
import com.github.alexthe666.iceandfire.entity.props.EntityDataProvider;
import com.github.alexthe666.iceandfire.entity.util.DragonUtils;
import com.github.alexthe666.iceandfire.entity.util.IBlacklistedFromStatues;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

public class ItemCockatriceScepter extends Item {

    private final Random rand = new Random();
    // The wear of a use lives in the caster's MiscData (scepterWear): a field here was shared by all players

    public ItemCockatriceScepter() {
        super(new Item.Properties()/*.tab(IceAndFire.TAB_ITEMS)*/.durability(700));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Item.TooltipContext worldIn, List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        tooltip.add(Component.translatable("item.iceandfire.legendary_weapon.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.iceandfire.cockatrice_scepter.desc_0").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.iceandfire.cockatrice_scepter.desc_1").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level worldIn, @NotNull LivingEntity livingEntity, int timeLeft) {
        EntityDataProvider.getExisting(livingEntity).ifPresent(data -> {
            if (data.miscData.scepterWear > 0) {
                stack.hurtAndBreak(data.miscData.scepterWear, livingEntity, LivingEntity.getSlotForHand(livingEntity.getUsedItemHand()));
                data.miscData.scepterWear = 0;
            }
            data.miscData.getTargetedByScepter().clear();
        });
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return 1;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level worldIn, Player playerIn, @NotNull InteractionHand hand) {
        ItemStack itemStackIn = playerIn.getItemInHand(hand);
        playerIn.startUsingItem(hand);
        return new InteractionResultHolder<>(InteractionResult.PASS, itemStackIn);
    }

    @Override
    public void onUseTick(Level level, LivingEntity player, ItemStack stack, int count) {
        if (player instanceof Player) {
            double dist = 32;
            Vec3 playerEyePosition = player.getEyePosition(1.0F);
            Vec3 playerLook = player.getViewVector(1.0F);
            Vec3 Vector3d2 = playerEyePosition.add(playerLook.x * dist, playerLook.y * dist, playerLook.z * dist);
            Entity pointedEntity = null;
            List<Entity> nearbyEntities = level.getEntities(player, player.getBoundingBox().expandTowards(playerLook.x * dist, playerLook.y * dist, playerLook.z * dist).inflate(1.0D, 1.0D, 1.0D), new Predicate<Entity>() {
                @Override
                public boolean test(Entity entity) {
                    boolean blindness = entity instanceof LivingEntity && ((LivingEntity) entity).hasEffect(MobEffects.BLINDNESS) || (entity instanceof IBlacklistedFromStatues && !((IBlacklistedFromStatues) entity).canBeTurnedToStone());
                    return entity != null && entity.isPickable() && !blindness && (entity instanceof Player || (entity instanceof LivingEntity && DragonUtils.isAlive((LivingEntity) entity)));
                }
            });
            double d2 = dist;
            for (Entity nearbyEntity : nearbyEntities) {
                AABB axisalignedbb = nearbyEntity.getBoundingBox().inflate(nearbyEntity.getPickRadius());
                Optional<Vec3> optional = axisalignedbb.clip(playerEyePosition, Vector3d2);

                if (axisalignedbb.contains(playerEyePosition)) {
                    if (d2 >= 0.0D) {
                        pointedEntity = nearbyEntity;
                        d2 = 0.0D;
                    }
                } else if (optional.isPresent()) {
                    double d3 = playerEyePosition.distanceTo(optional.get());

                    if (d3 < d2 || d2 == 0.0D) {
                        if (nearbyEntity.getRootVehicle() == player.getRootVehicle() && !dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.canRiderInteract(player)) {
                            if (d2 == 0.0D) {
                                pointedEntity = nearbyEntity;
                            }
                        } else {
                            pointedEntity = nearbyEntity;
                            d2 = d3;
                        }
                    }
                }

            }
            if (pointedEntity instanceof LivingEntity target) {
                if (!target.isAlive()) {
                    return;
                }

                Player caster = (Player) player;
                EntityDataProvider.getCapability(player).ifPresent(data -> {
                    // Other players' pets, PvP off and claims stop the beam. Asked once a target, a refusal again at most
                    // once a second: the event every tick repeated a claim mod's refusal message 20 times a second
                    var misc = data.miscData;
                    if (misc.getTargetedByScepter().contains(target)) {
                        return;
                    }
                    long now = level.getGameTime();
                    if (now - misc.scepterRefusedAt >= 20 || now < misc.scepterRefusedAt) {
                        misc.scepterRefused.clear();
                        misc.scepterRefusedAt = now;
                    }
                    if (misc.scepterRefused.contains(target.getId())) {
                        return;
                    }
                    if (com.github.alexthe666.iceandfire.entity.util.IafAttackRules.mayHarm(caster, target)) {
                        misc.addScepterTarget(target);
                    } else {
                        misc.scepterRefused.add(target.getId());
                    }
                });
            }

            attackTargets(player);
        }
    }

    private void attackTargets(final LivingEntity caster) {
        EntityDataProvider.getCapability(caster).ifPresent(data -> {
            List<LivingEntity> targets = new ArrayList<>(data.miscData.getTargetedByScepter());

            for (LivingEntity target : targets) {
                if (!EntityGorgon.isEntityLookingAt(caster, target, 0.2F) || !caster.isAlive() || !target.isAlive()) {
                    data.miscData.removeScepterTarget(target);
                    continue;
                }

                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 2));

                if (caster.tickCount % 20 == 0) {
                    data.miscData.scepterWear++;
                    // with the caster as the attacker: without one, PvP off, kill credit and claim mods missed it
                    target.hurt(new net.minecraft.world.damagesource.DamageSource(caster.level().registryAccess()
                        .lookupOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getOrThrow(net.minecraft.world.damagesource.DamageTypes.WITHER), caster), 2);
                }

                drawParticleBeam(caster, target);
            }
        });
    }

    private void drawParticleBeam(LivingEntity origin, LivingEntity target) {
        double d5 = 80F;
        double d0 = target.getX() - origin.getX();
        double d1 = target.getY() + (double) (target.getBbHeight() * 0.5F)
            - (origin.getY() + (double) origin.getEyeHeight() * 0.5D);
        double d2 = target.getZ() - origin.getZ();
        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
        d0 = d0 / d3;
        d1 = d1 / d3;
        d2 = d2 / d3;
        double d4 = this.rand.nextDouble();
        while (d4 < d3) {
            d4 += 1.0D;
            origin.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0xFFFFFFFF), origin.getX() + d0 * d4, origin.getY() + d1 * d4 + (double) origin.getEyeHeight() * 0.5D, origin.getZ() + d2 * d4, 0.0D, 0.0D, 0.0D);
        }
    }

}
