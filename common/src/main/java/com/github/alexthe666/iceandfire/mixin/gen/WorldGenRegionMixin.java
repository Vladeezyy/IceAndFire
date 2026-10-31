package com.github.alexthe666.iceandfire.mixin.gen;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/** Avoid log spam for dragon caves (some blocks can exceed the writable worldgen area due to their size) */
@Mixin(WorldGenRegion.class)
public class WorldGenRegionMixin {
    @Inject(method = "ensureCanWrite", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;logAndPauseIfInIde(Ljava/lang/String;)V", shift = At.Shift.BEFORE), cancellable = true)
    private void skipLog(final BlockPos position, final CallbackInfoReturnable<Boolean> callback) {
        if (this.currentlyGenerating != null && this.currentlyGenerating.get().contains(IceAndFire.MODID)) {
            callback.setReturnValue(false);
        }
    }

    /**
     * 26.2 also logs every read outside the write zone (1.21.1 allowed it silently; the read still happens): the dragon
     * caves and roosts read their surroundings, hundreds of thousands of lines and a string built for each read.
     */
    @Inject(method = "warnIfReadOutsideWriteZone", at = @At("HEAD"), cancellable = true)
    private void skipReadLog(final int chunkX, final int chunkZ, final CallbackInfo callback) {
        if (this.currentlyGenerating != null && this.currentlyGenerating.get().contains(IceAndFire.MODID)) {
            callback.cancel();
        }
    }

    @Shadow @Nullable private Supplier<String> currentlyGenerating;
}
