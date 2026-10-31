package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;

/** Entity data types of the mod: 26.x removed EntityDataSerializers.COMPOUND_TAG (the stone statue syncs its mob as NBT) */
public final class IafDataSerializers {
    public static final EntityDataSerializer<CompoundTag> COMPOUND_TAG = EntityDataSerializer.forValueType(ByteBufCodecs.COMPOUND_TAG);

    private IafDataSerializers() {}

    public static void register() {
        dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.registerDataSerializer(Identifier.fromNamespaceAndPath(IceAndFire.MODID, "compound_tag"), COMPOUND_TAG);
    }
}
