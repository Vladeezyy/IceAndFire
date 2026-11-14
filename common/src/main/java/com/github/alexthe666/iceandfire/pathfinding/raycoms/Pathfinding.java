package com.github.alexthe666.iceandfire.pathfinding.raycoms;
/*
    All of this code is used with permission from Raycoms, one of the developers of the minecolonies project.
 */

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.thread.BlockableEventLoop;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;


/**
 * Static class the handles all the Pathfinding.
 */
public final class Pathfinding {
    private static final BlockingQueue<Runnable> jobQueue = new LinkedBlockingDeque<>();
    private static ThreadPoolExecutor executor;

    private Pathfinding() {
        //Hides default constructor.
    }

    public static boolean isDebug() {
        return IafConfig.pathfindingDebug;
    }

    /**
     * Creates a new thread pool for pathfinding jobs
     *
     * @return the threadpool executor.
     */
    public static ThreadPoolExecutor getExecutor() {
        if (executor == null) {
            // A pool with an unbounded queue never grows past its core size: core 1 ran every path on one thread whatever
            // the config said. Core = max, up to the cores of the machine; idle threads still end after 10 s
            int threads = Math.max(1, Math.min(IafConfig.dragonPathfindingThreads, Runtime.getRuntime().availableProcessors()));
            executor = new ThreadPoolExecutor(threads, threads, 10, TimeUnit.SECONDS, jobQueue, new IafThreadFactory());
            executor.allowCoreThreadTimeOut(true);
        }
        return executor;
    }

    /**
     * Ice and Fire specific thread factory.
     */
    public static class IafThreadFactory implements ThreadFactory {
        /**
         * Ongoing thread IDs.
         */
        public static int id;

        @Override
        public Thread newThread(final @NotNull Runnable runnable) throws RuntimeException {
            BlockableEventLoop<?> workqueue = dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.currentServer();
            ClassLoader classLoader;
            if (workqueue.isSameThread()) {
                classLoader = Thread.currentThread().getContextClassLoader();
            } else if (workqueue instanceof MinecraftServer server){
               classLoader = server.getRunningThread().getContextClassLoader();
            } else {
                classLoader = CompletableFuture.supplyAsync(() -> Thread.currentThread().getContextClassLoader(), workqueue).orTimeout(10, TimeUnit.SECONDS).exceptionally((ex)-> {
                    throw new RuntimeException(String.format("Couldn't join threads within timeout range. Tried joining '%s' on '%s'", Thread.currentThread().getName(), workqueue.name()));
                }).join();
            }
            final Thread thread = new Thread(runnable, "Ice and Fire Pathfinding Worker #" + (id++));
            thread.setDaemon(true);
            thread.setPriority(Thread.MAX_PRIORITY);
            if (thread.getContextClassLoader() != classLoader) {
                IceAndFire.LOGGER.info("Corrected CCL of new Ice and Fire Pathfinding Thread, was: " + thread.getContextClassLoader().toString());
                thread.setContextClassLoader(classLoader);
            }
            thread.setUncaughtExceptionHandler((thread1, throwable) -> IceAndFire.LOGGER.error("Ice and Fire Pathfinding Thread errored! ", throwable));
            return thread;
        }
    }
}
