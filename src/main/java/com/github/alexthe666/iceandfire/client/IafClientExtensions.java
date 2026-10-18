package com.github.alexthe666.iceandfire.client;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.item.IafClientItem;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

@EventBusSubscriber(modid = IceAndFire.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class IafClientExtensions {

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        for (DeferredHolder<Item, ? extends Item> holder : IafItemRegistry.ITEMS.getEntries()) {
            Item item = holder.get();
            if (item instanceof IafClientItem clientItem) {
                clientItem.initializeClient(extensions -> event.registerItem(extensions, item));
            }
        }
    }
}
