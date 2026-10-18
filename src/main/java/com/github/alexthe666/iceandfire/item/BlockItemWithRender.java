package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.citadel.client.CitadelItemRenderProperties;
import com.github.alexthe666.iceandfire.client.render.tile.IceAndFireTEISR;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.util.Lazy;

import java.util.function.Consumer;

public class BlockItemWithRender extends BlockItem implements IafClientItem {
    public BlockItemWithRender(Block p_40565_, Properties p_40566_) {
        super(p_40565_, p_40566_);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new CitadelItemRenderProperties() {
            static final Lazy<BlockEntityWithoutLevelRenderer> renderer = Lazy.of(() -> new IceAndFireTEISR(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()));

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer.get();
            }
        });
    }

}
