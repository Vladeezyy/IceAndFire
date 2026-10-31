package com.github.alexthe666.iceandfire.client.render.entity;

import com.github.alexthe666.iceandfire.client.model.ModelTroll;
import com.github.alexthe666.iceandfire.client.render.entity.layer.LayerTrollEyes;
import com.github.alexthe666.iceandfire.client.render.entity.layer.LayerTrollWeapon;
import com.github.alexthe666.iceandfire.entity.EntityTroll;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class RenderTroll extends IafMobRenderer<EntityTroll, ModelTroll> {

    public RenderTroll(EntityRendererProvider.Context context) {
        super(context, new ModelTroll(), 0.9F);
        this.addLayer(new LayerTrollWeapon(this));
        this.addLayer(new LayerTrollEyes(this));
    }

    @Override
    protected Identifier getTexture(EntityTroll troll) {
        return troll.getTrollType().TEXTURE;
    }
}
