package dev.vladeezyy.iceandfire.fabric.mixin;

import com.github.alexthe666.iceandfire.item.ItemCyclopsEye;
import com.github.alexthe666.iceandfire.item.ItemDeathwormGauntlet;
import com.github.alexthe666.iceandfire.item.ItemHydraHeart;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

/** NeoForge's shouldCauseReequipAnimation: items whose data changes every tick do not bob in the hand */
@Mixin({ItemCyclopsEye.class, ItemDeathwormGauntlet.class, ItemHydraHeart.class})
public abstract class ReequipItemMixin implements FabricItem {
    @Override
    public boolean allowComponentsUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack) {
        return !ItemStack.isSameItem(oldStack, newStack);
    }
}
