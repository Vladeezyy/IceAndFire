package com.github.alexthe666.iceandfire.entity.props;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * The Forge capability of the original ("iceandfire:entity_data") as a NeoForge data attachment.
 */
public class IafAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, IceAndFire.MODID);

    public static final Supplier<AttachmentType<EntityData>> ENTITY_DATA = ATTACHMENTS.register("entity_data",
            () -> AttachmentType.serializable(EntityData::new).build());
}
