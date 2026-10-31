package dev.vladeezyy.iceandfire.neoforge;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.props.EntityData;
import com.github.alexthe666.iceandfire.entity.util.Values;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
        () -> AttachmentType.builder(EntityData::new).serialize(new IAttachmentSerializer<EntityData>() {
            // the same NBT as 1.21.1 (one compound per part, under the attachment's key)
            private static final String[] KEYS = {"frozenData", "chainedData", "sirenData", "chickenData", "miscData"};

            @Override
            public EntityData read(IAttachmentHolder holder, ValueInput input) {
                CompoundTag tag = new CompoundTag();
                for (String key : KEYS) {
                    Tag part = Values.get(input, key);
                    if (part != null) {
                        tag.put(key, part);
                    }
                }
                EntityData data = new EntityData();
                data.deserialize(tag);
                return data;
            }

            @Override
            public boolean write(EntityData data, ValueOutput output) {
                CompoundTag tag = data.serialize();
                for (String key : tag.keySet()) {
                    Values.put(output, key, tag.get(key));
                }
                return true;
            }
        }).build());

    private IafAttachments() {
    }
}
