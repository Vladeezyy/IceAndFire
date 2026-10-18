package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.IafConfig;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;

public class DragonsteelArmorMaterial extends IafArmorMaterial {

    public DragonsteelArmorMaterial(String name, int durability, int[] damageReduction, int encantability, Holder<SoundEvent> sound, float toughness) {
        super(name, durability, damageReduction, encantability, sound, toughness);
    }

    @Override
    public int getDefenseForType(ArmorItem.Type slotIn) {
        int[] damageReduction = new int[]{IafConfig.dragonsteelBaseArmor - 6, IafConfig.dragonsteelBaseArmor - 3, IafConfig.dragonsteelBaseArmor, IafConfig.dragonsteelBaseArmor - 5};
        return damageReduction[index(slotIn)];
    }

    @Override
    public float getKnockbackResistance(){
        return 0;
    }

    @Override
    public float getToughness() {
        return IafConfig.dragonsteelBaseArmorToughness;
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type slotIn) {
        return (int) (MAX_DAMAGE_ARRAY[index(slotIn)] * 0.02D * IafConfig.dragonsteelBaseDurabilityEquipment);
    }
}
