package com.github.alexthe666.iceandfire.client.model.armor;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jetbrains.annotations.NotNull;


public class ArmorModelBase extends HumanoidModel<HumanoidRenderState> {
    protected static float INNER_MODEL_OFFSET = 0.38F;
    protected static float OUTER_MODEL_OFFSET = 0.45F;
    /** Above the hat layer of the player model (0.5): the inner offset of 1.20.1 let the skin through helmets (#5112). */
    protected static float HEAD_MODEL_OFFSET = 0.55F;
    public ArmorModelBase(ModelPart p_170677_) {
        super(p_170677_);
    }

    // 26.x: posed from the humanoid render state (an armor stand's state carries its part poses)
    @Override
    public void setupAnim(@NotNull HumanoidRenderState state) {
        if (state instanceof ArmorStandRenderState armorStand) {
            pose(this.head, armorStand.headPose);
            pose(this.body, armorStand.bodyPose);
            pose(this.leftArm, armorStand.leftArmPose);
            pose(this.rightArm, armorStand.rightArmPose);
            pose(this.leftLeg, armorStand.leftLegPose);
            pose(this.rightLeg, armorStand.rightLegPose);
        } else {
            super.setupAnim(state);
        }
    }

    private static void pose(ModelPart part, net.minecraft.core.Rotations rotations) {
        part.xRot = ((float) Math.PI / 180F) * rotations.x();
        part.yRot = ((float) Math.PI / 180F) * rotations.y();
        part.zRot = ((float) Math.PI / 180F) * rotations.z();
    }

    //this.(?<name>.*).addChild\(this.(?<name2>.*)\);
    //partdefinition.getChild("${name}").addOrReplaceChild("${name2},

    //this.(?<name>.*) = new AdvancedModelBox\(.*, (?<texX>[0-9]*), (?<texY>[0-9]*)\);
    //.addOrReplaceChild("${name}", CubeListBuilder.create().texOffs(${texX}, ${texY})

    //this.(?<name>.*).setPos\((?<x>.*), (?<y>.*), (?<z>.*)\);
    //PartPose.offsetAndRotation(${x}, ${y}, ${z},

    //this.(?<name>.*).addBox\((?<x>.*), (?<y>.*), (?<z>.*), (?<u>.*), (?<v>.*), (?<w>.*), 0.0F\);
    //.addBox(${x}, ${y}, ${z}, ${u}, ${v}, ${w})

    //(?<main>.addOrReplaceChild\("(?<name>.*)", Cube.*)\n.*(?<part>PartPose.*)\n.*(?<box>addBox.*)\n.*this.setRotateAngle\(.*\k<name>.*, (?<aX>.*), (?<aY>.*), (?<aZ>.*)\);
    //${main}.${box}, ${part}${aX}, ${aY}, ${aZ}));

    //(?<main>.addOrReplaceChild\("(?<name>.*)", Cube.*)\n.*(?<part>PartPose.*)\n.*(?<box>addBox.*\));\n
    //${main}.${box}, ${part});
}
