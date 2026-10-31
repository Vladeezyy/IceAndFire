package com.github.alexthe666.iceandfire.client.model;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import com.github.alexthe666.iceandfire.entity.EntityStoneStatue;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import org.jetbrains.annotations.NotNull;

public class ModelStonePlayer extends HumanoidModel<HumanoidRenderState> {

    public ModelStonePlayer(ModelPart p_170677_) {
        super(p_170677_);
    }

    @Override
    public void setupAnim(@NotNull HumanoidRenderState state) {
    }
}
