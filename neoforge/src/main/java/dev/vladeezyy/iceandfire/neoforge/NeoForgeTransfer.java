package dev.vladeezyy.iceandfire.neoforge;

import dev.vladeezyy.iceandfire.platform.transfer.ItemHandler;
import dev.vladeezyy.iceandfire.platform.transfer.ModifiableItemHandler;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

/** The common item handlers of Ice and Fire as NeoForge capabilities and back (wrappers unwrap each other) */
public final class NeoForgeTransfer {
    private NeoForgeTransfer() {}


    @Nullable
    public static IItemHandler toNeo(@Nullable ItemHandler handler) {
        if (handler == null) {
            return null;
        }
        if (handler instanceof FromNeoItems wrapped) {
            return wrapped.neo;
        }
        return handler instanceof ModifiableItemHandler modifiable ? new ToNeoModifiableItems(modifiable) : new ToNeoItems(handler);
    }

    @Nullable
    public static ItemHandler fromNeo(@Nullable IItemHandler handler) {
        if (handler == null) {
            return null;
        }
        if (handler instanceof ToNeoItems wrapped) {
            return wrapped.handler;
        }
        return new FromNeoItems(handler);
    }

    static class ToNeoItems implements IItemHandler {
        final ItemHandler handler;

        ToNeoItems(ItemHandler handler) {
            this.handler = handler;
        }

        @Override public int getSlots() { return this.handler.getSlots(); }
        @Override public ItemStack getStackInSlot(int slot) { return this.handler.getStackInSlot(slot); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return this.handler.insertItem(slot, stack, simulate); }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return this.handler.extractItem(slot, amount, simulate); }
        @Override public int getSlotLimit(int slot) { return this.handler.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return this.handler.isItemValid(slot, stack); }
    }

    static final class ToNeoModifiableItems extends ToNeoItems implements IItemHandlerModifiable {
        ToNeoModifiableItems(ModifiableItemHandler handler) {
            super(handler);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            ((ModifiableItemHandler) this.handler).setStackInSlot(slot, stack);
        }
    }

    static final class FromNeoItems implements ModifiableItemHandler {
        final IItemHandler neo;

        FromNeoItems(IItemHandler neo) {
            this.neo = neo;
        }

        @Override public int getSlots() { return this.neo.getSlots(); }
        @Override public ItemStack getStackInSlot(int slot) { return this.neo.getStackInSlot(slot); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return this.neo.insertItem(slot, stack, simulate); }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return this.neo.extractItem(slot, amount, simulate); }
        @Override public int getSlotLimit(int slot) { return this.neo.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return this.neo.isItemValid(slot, stack); }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            if (this.neo instanceof IItemHandlerModifiable modifiable) {
                modifiable.setStackInSlot(slot, stack);
            }
        }
    }

}
