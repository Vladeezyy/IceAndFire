package dev.vladeezyy.iceandfire.fabric;

import com.github.alexthe666.iceandfire.entity.tile.TileEntityJar;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

/** Pixie dust out of the bottom of a jar with a pixie, as PixieJarInvWrapper on NeoForge; transactional through the produced flag */
final class PixieJarStorage extends SnapshotParticipant<Boolean> implements ExtractionOnlyStorage<ItemVariant>, SingleSlotStorage<ItemVariant> {
    private final TileEntityJar jar;

    PixieJarStorage(TileEntityJar jar) {
        this.jar = jar;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        if (maxAmount > 0 && this.jar.hasProduced && resource.isOf(IafItemRegistry.PIXIE_DUST.get())) {
            this.updateSnapshots(transaction);
            this.jar.hasProduced = false;
            return 1;
        }
        return 0;
    }

    @Override
    public boolean isResourceBlank() {
        return !this.jar.hasProduced;
    }

    @Override
    public ItemVariant getResource() {
        return this.jar.hasProduced ? ItemVariant.of(IafItemRegistry.PIXIE_DUST.get()) : ItemVariant.blank();
    }

    @Override
    public long getAmount() {
        return this.jar.hasProduced ? 1 : 0;
    }

    @Override
    public long getCapacity() {
        return 1;
    }

    @Override
    protected Boolean createSnapshot() {
        return this.jar.hasProduced;
    }

    @Override
    protected void readSnapshot(Boolean snapshot) {
        this.jar.hasProduced = snapshot;
    }

    @Override
    protected void onFinalCommit() {
        this.jar.setChanged();
    }
}
