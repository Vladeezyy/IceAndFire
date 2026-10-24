package dev.vladeezyy.iceandfire.bus;

/** Posted once when the mod has registered its content (NeoForge: common setup, Fabric: mod init) */
public class FMLCommonSetupEvent extends Event {
    public void enqueueWork(Runnable work) {
        work.run();
    }
}
