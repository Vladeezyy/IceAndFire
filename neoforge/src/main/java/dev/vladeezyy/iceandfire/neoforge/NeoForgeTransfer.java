package dev.vladeezyy.iceandfire.neoforge;

import dev.vladeezyy.iceandfire.platform.transfer.ItemHandler;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The common item handlers of Ice and Fire as NeoForge 26.x resource handlers (Capabilities.Item):
 * transactions are journaled per slot and applied to the common handler on the root commit.
 */
public final class NeoForgeTransfer {
    /** One adapter per common handler (hoppers ask every few ticks) */
    private static final Map<ItemHandler, Items> ADAPTERS = new WeakHashMap<>();

    private NeoForgeTransfer() {}

    @Nullable
    public static synchronized ResourceHandler<ItemResource> toNeo(@Nullable ItemHandler handler) {
        if (handler == null) {
            return null;
        }
        Items adapter = ADAPTERS.get(handler);
        if (adapter == null || adapter.size() != handler.getSlots()) {
            adapter = new Items(handler);
            ADAPTERS.put(handler, adapter);
        }
        return adapter;
    }

    private static int clamp(long value) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, value));
    }

    private record SlotState(ItemResource resource, int in, int out) {}

    static final class Items implements ResourceHandler<ItemResource> {
        private final ItemHandler handler;
        private final List<Slot> slots = new ArrayList<>();

        Items(ItemHandler handler) {
            this.handler = handler;
            for (int i = 0; i < handler.getSlots(); i++) this.slots.add(new Slot(handler, i));
        }

        @Override
        public int size() {
            return this.slots.size();
        }

        @Override
        public ItemResource getResource(int index) {
            return this.slots.get(index).resource();
        }

        @Override
        public long getAmountAsLong(int index) {
            return this.slots.get(index).amount();
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            int limit = this.handler.getSlotLimit(index);
            return resource.isEmpty() ? limit : Math.min(limit, resource.getMaxStackSize());
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return this.handler.isItemValid(index, resource.toStack());
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return this.slots.get(index).insert(resource, amount, transaction);
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return this.slots.get(index).extract(resource, amount, transaction);
        }
    }

    private static final class Slot extends SnapshotJournal<SlotState> {
        private final ItemHandler handler;
        private final int slot;
        private ItemResource pending = ItemResource.EMPTY;
        private int in;
        private int out;

        Slot(ItemHandler handler, int slot) {
            this.handler = handler;
            this.slot = slot;
        }

        ItemResource resource() {
            ItemStack current = this.handler.getStackInSlot(this.slot);
            if (current.isEmpty()) return this.in > 0 ? this.pending : ItemResource.EMPTY;
            return current.getCount() - this.out <= 0 ? ItemResource.EMPTY : ItemResource.of(current);
        }

        int amount() {
            return Math.max(0, this.handler.getStackInSlot(this.slot).getCount() + this.in - this.out);
        }

        int insert(ItemResource resource, int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            if (this.out > 0 || this.in > 0 && !resource.equals(this.pending)) return 0;
            int wanted = clamp((long) this.in + amount);
            ItemStack rest = this.handler.insertItem(this.slot, resource.toStack(wanted), true);
            int accepted = Math.min(amount, wanted - rest.getCount() - this.in);
            if (accepted <= 0) return 0;
            this.updateSnapshots(transaction);
            this.pending = resource;
            this.in += accepted;
            return accepted;
        }

        int extract(ItemResource resource, int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            if (this.in > 0) return 0;
            ItemStack current = this.handler.getStackInSlot(this.slot);
            if (current.isEmpty() || !resource.matches(current)) return 0;
            ItemStack taken = this.handler.extractItem(this.slot, clamp((long) this.out + amount), true);
            int extracted = Math.min(amount, taken.getCount() - this.out);
            if (extracted <= 0) return 0;
            this.updateSnapshots(transaction);
            this.out += extracted;
            return extracted;
        }

        @Override
        protected SlotState createSnapshot() {
            return new SlotState(this.pending, this.in, this.out);
        }

        @Override
        protected void revertToSnapshot(SlotState snapshot) {
            this.pending = snapshot.resource();
            this.in = snapshot.in();
            this.out = snapshot.out();
        }

        @Override
        protected void onRootCommit(SlotState original) {
            int inserted = this.in;
            int extracted = this.out;
            ItemResource resource = this.pending;
            this.in = 0;
            this.out = 0;
            this.pending = ItemResource.EMPTY;
            if (inserted > 0) this.handler.insertItem(this.slot, resource.toStack(inserted), false);
            if (extracted > 0) this.handler.extractItem(this.slot, extracted, false);
        }
    }
}
