package com.github.alexthe666.iceandfire.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * InventoryScreen#renderEntityInInventoryFollowsMouse of 1.20.1: the entity stands at (x, y) and looks at the mouse.
 * The 1.21 version takes a box and clips the entity to it, which cuts off the bigger mounts.
 */
public final class GuiEntityRenderUtil {

    private GuiEntityRenderUtil() {
    }

    public static void renderEntityFollowsMouse(GuiGraphics guiGraphics, int x, int y, int scale, float mouseX, float mouseY, LivingEntity entity) {
        float f = (float) Math.atan(mouseX / 40.0F);
        float f1 = (float) Math.atan(mouseY / 40.0F);
        Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf quaternionf1 = new Quaternionf().rotateX(f1 * 20.0F * ((float) Math.PI / 180F));
        quaternionf.mul(quaternionf1);
        float yBodyRot = entity.yBodyRot;
        float yRot = entity.getYRot();
        float xRot = entity.getXRot();
        float yHeadRotO = entity.yHeadRotO;
        float yHeadRot = entity.yHeadRot;
        entity.yBodyRot = 180.0F + f * 20.0F;
        entity.setYRot(180.0F + f * 40.0F);
        entity.setXRot(-f1 * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();
        InventoryScreen.renderEntityInInventory(guiGraphics, x, y, scale, new Vector3f(), quaternionf, quaternionf1, entity);
        entity.yBodyRot = yBodyRot;
        entity.setYRot(yRot);
        entity.setXRot(xRot);
        entity.yHeadRotO = yHeadRotO;
        entity.yHeadRot = yHeadRot;
    }
}
