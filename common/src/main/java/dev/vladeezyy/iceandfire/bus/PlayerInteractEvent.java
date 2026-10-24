package dev.vladeezyy.iceandfire.bus;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public abstract class PlayerInteractEvent extends PlayerEvent {
    private final InteractionHand hand;
    private final BlockPos pos;
    @Nullable
    private final Direction face;
    private InteractionResult cancellationResult = InteractionResult.PASS;

    protected PlayerInteractEvent(Player player, InteractionHand hand, BlockPos pos, @Nullable Direction face) {
        super(player);
        this.hand = hand;
        this.pos = pos;
        this.face = face;
    }

    public InteractionHand getHand() {
        return this.hand;
    }

    public ItemStack getItemStack() {
        return this.getEntity().getItemInHand(this.hand);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    @Nullable
    public Direction getFace() {
        return this.face;
    }

    public Level getLevel() {
        return this.getEntity().level();
    }

    /** The result the interaction returns when the event is canceled */
    public InteractionResult getCancellationResult() {
        return this.cancellationResult;
    }

    public void setCancellationResult(InteractionResult result) {
        this.cancellationResult = result;
    }

    /** A player right-clicks an entity (both sides) */
    public static class EntityInteract extends PlayerInteractEvent implements ICancellableEvent {
        private final Entity target;

        public EntityInteract(Player player, InteractionHand hand, Entity target) {
            super(player, hand, target.blockPosition(), null);
            this.target = target;
        }

        public Entity getTarget() {
            return this.target;
        }
    }

    /** A player right-clicks a block (both sides) */
    public static class RightClickBlock extends PlayerInteractEvent implements ICancellableEvent {
        public RightClickBlock(Player player, InteractionHand hand, BlockPos pos, @Nullable Direction face) {
            super(player, hand, pos, face);
        }
    }

    /** A player right-clicks with an item, not at a block or entity (both sides) */
    public static class RightClickItem extends PlayerInteractEvent implements ICancellableEvent {
        public RightClickItem(Player player, InteractionHand hand) {
            super(player, hand, player.blockPosition(), null);
        }
    }

    /** A player left-clicks the air (client only, empty hand or not) */
    public static class LeftClickEmpty extends PlayerInteractEvent {
        public LeftClickEmpty(Player player) {
            super(player, InteractionHand.MAIN_HAND, player.blockPosition(), null);
        }
    }
}
