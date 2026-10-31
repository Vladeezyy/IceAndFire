package com.github.alexthe666.iceandfire.client.render;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.client.IafItemRenderers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * The items the mod draws in code (1.21.1: builtin/entity models with a BlockEntityWithoutLevelRenderer): a special
 * item model {@code iceandfire:item_renderer} that runs the 1.21.1 renderByItem into an {@link IafBufferSource}. The item
 * definitions select one model per display context (field {@code display}, as 26.x special models get no context);
 * without the field the context of the call is used.
 */
public class IafItemSpecialRenderer implements SpecialModelRenderer<ItemStack> {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(IceAndFire.MODID, "item_renderer");
    private final Kind kind;
    private final ItemDisplayContext display;

    public IafItemSpecialRenderer(Kind kind, ItemDisplayContext display) {
        this.kind = kind;
        this.display = display;
    }

    @Override
    public void submit(@Nullable ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay, boolean hasFoil, int outlineColor) {
        if (stack == null) {
            return;
        }
        IafBufferSource buffers = new IafBufferSource();
        IafItemRenderers.renderer(this.kind).renderByItem(stack, this.display != ItemDisplayContext.NONE ? this.display : displayContext, poseStack, buffers, light, overlay);
        buffers.submit(collector);
    }

    @Override
    public ItemStack extractArgument(ItemStack stack) {
        return stack.copy();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        // a block-sized cube, as the 1.21.1 builtin/entity items were drawn in
        for (int i = 0; i < 8; i++) {
            output.accept(new Vector3f(i & 1, (i >> 1) & 1, (i >> 2) & 1));
        }
    }

    public enum Kind implements StringRepresentable {
        BLOCK_ITEMS("block_items"),
        GAUNTLET("deathworm_gauntlet"),
        GORGON_HEAD("gorgon_head"),
        TIDE_TRIDENT("tide_trident"),
        TROLL_WEAPON("troll_weapon");

        public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);
        private final String name;

        Kind(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public record Unbaked(Kind kind, ItemDisplayContext display) implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Kind.CODEC.fieldOf("renderer").forGetter(Unbaked::kind),
            ItemDisplayContext.CODEC.optionalFieldOf("display", ItemDisplayContext.NONE).forGetter(Unbaked::display)
        ).apply(i, Unbaked::new));

        @Override
        public SpecialModelRenderer<ItemStack> bake(SpecialModelRenderer.BakingContext context) {
            return new IafItemSpecialRenderer(this.kind, this.display);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
