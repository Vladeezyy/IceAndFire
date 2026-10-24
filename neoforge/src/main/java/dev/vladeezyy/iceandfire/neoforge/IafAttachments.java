package dev.vladeezyy.iceandfire.neoforge;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.props.EntityData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/** The Forge capability of the original ("iceandfire:entity_data") as a NeoForge data attachment */
final class IafAttachments {
    static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, IceAndFire.MODID);

    static final Supplier<AttachmentType<EntityData>> ENTITY_DATA = ATTACHMENTS.register("entity_data",
        () -> AttachmentType.builder(EntityData::new).serialize(new IAttachmentSerializer<CompoundTag, EntityData>() {
            @Override
            public EntityData read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
                EntityData data = new EntityData();
                data.deserialize(tag);
                return data;
            }

            @Override
            public CompoundTag write(EntityData data, HolderLookup.Provider provider) {
                return data.serialize();
            }
        }).build());

    private IafAttachments() {
    }
}
