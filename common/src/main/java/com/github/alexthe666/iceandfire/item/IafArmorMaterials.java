package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ArmorMaterial;
import dev.vladeezyy.iceandfire.platform.Registrar;

public class IafArmorMaterials {
    public static final Registrar<ArmorMaterial> MATERIALS = Registrar.create(Registries.ARMOR_MATERIAL, IceAndFire.MODID);
}
