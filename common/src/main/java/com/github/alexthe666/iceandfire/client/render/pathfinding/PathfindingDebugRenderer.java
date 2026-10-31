package com.github.alexthe666.iceandfire.client.render.pathfinding;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.MNode;
import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;

/**
 * The pathfinding debug view (the original's, from Minecolonies): a small box per node, a line to its parent and the
 * cost of the nodes near the camera. 26.x draws it with vanilla's debug gizmos (the original drew its own quads).
 */
public class PathfindingDebugRenderer {
    /**
     * Set of visited nodes.
     */
    public static Set<MNode> lastDebugNodesVisited = new HashSet<>();

    /**
     * Set of not visited nodes.
     */
    public static Set<MNode> lastDebugNodesNotVisited = new HashSet<>();

    /**
     * Set of nodes that belong to the chosen path.
     */
    public static Set<MNode> lastDebugNodesPath = new HashSet<>();

    /**
     * Adds the debug view of the last synced path to the frame's gizmos (call inside a gizmo collection).
     */
    public static void render() {
        if (lastDebugNodesVisited.isEmpty() && lastDebugNodesNotVisited.isEmpty() && lastDebugNodesPath.isEmpty()) {
            return;
        }
        try {
            for (final MNode n : lastDebugNodesVisited) {
                debugDrawNode(n, 0xffff0000);
            }

            for (final MNode n : lastDebugNodesNotVisited) {
                debugDrawNode(n, 0xff0000ff);
            }

            for (final MNode n : lastDebugNodesPath) {
                if (n.isReachedByWorker()) {
                    debugDrawNode(n, 0xffff6600);
                } else {
                    debugDrawNode(n, 0xff00ff00);
                }
            }
        } catch (final ConcurrentModificationException exc) {
            IceAndFire.LOGGER.catching(exc);
        }
    }

    private static void debugDrawNode(final MNode n, final int argbColor) {
        final double x = n.pos.getX() + 0.375D, y = n.pos.getY() + 0.375D, z = n.pos.getZ() + 0.375D;
        Gizmos.cuboid(new AABB(x, y, z, x + 0.25D, y + 0.25D, z + 0.25D), GizmoStyle.fill(argbColor));

        if (n.parent != null) {
            final Vec3 center = new Vec3(x + 0.125D, y + 0.125D, z + 0.125D);
            final Vec3 parent = new Vec3(n.parent.pos.getX() + 0.5D, n.parent.pos.getY() + 0.5D, n.parent.pos.getZ() + 0.5D);
            Gizmos.line(center, parent, 0xffbfbfbf);
        }

        final Entity entity = Minecraft.getInstance().getCameraEntity();
        if (entity != null && n.pos.closerThan(entity.blockPosition(), 5d)) {
            renderDebugText(n, x + 0.125D, y + 0.75D, z + 0.125D);
        }
    }

    private static void renderDebugText(@NotNull final MNode n, double x, double y, double z) {
        final String s1 = String.format("F: %.3f [%d]", n.getCost(), n.getCounterAdded());
        final String s2 = String.format("G: %.3f [%d]", n.getScore(), n.getCounterVisited());
        Gizmos.billboardText(s1, new Vec3(x, y + 0.12D, z), TextGizmo.Style.whiteAndCentered());
        Gizmos.billboardText(s2, new Vec3(x, y, z), TextGizmo.Style.whiteAndCentered());
    }
}
