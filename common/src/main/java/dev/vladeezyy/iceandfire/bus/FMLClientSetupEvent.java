package dev.vladeezyy.iceandfire.bus;

/** Posted once on the client after the mod's content is registered */
public class FMLClientSetupEvent extends Event {
    public void enqueueWork(Runnable work) {
        work.run();
    }
}
