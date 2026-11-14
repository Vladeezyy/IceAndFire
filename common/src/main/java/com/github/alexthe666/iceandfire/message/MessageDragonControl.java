package com.github.alexthe666.iceandfire.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import dev.vladeezyy.iceandfire.platform.PacketContext;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.*;
import com.github.alexthe666.iceandfire.event.ServerEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;


public class MessageDragonControl implements CustomPacketPayload {
    public static final Type<MessageDragonControl> TYPE = new Type<>(Identifier.fromNamespaceAndPath(IceAndFire.MODID, "dragon_control"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageDragonControl> CODEC = StreamCodec.of((buf, message) -> write(message, buf), MessageDragonControl::read);

    @Override
    public Type<MessageDragonControl> type() {
        return TYPE;
    }

    public int dragonId;
    public byte controlState;
    public int armor_type;
    private double posX;
    private double posY;
    private double posZ;

    public MessageDragonControl(int dragonId, byte controlState, double posX, double posY, double posZ) {
        this.dragonId = dragonId;
        this.controlState = controlState;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
    }

    public MessageDragonControl() {
    }

    public static MessageDragonControl read(FriendlyByteBuf buf) {
        return new MessageDragonControl(buf.readInt(), buf.readByte(), buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public static void write(MessageDragonControl message, FriendlyByteBuf buf) {
        buf.writeInt(message.dragonId);
        buf.writeByte(message.controlState);
        buf.writeDouble(message.posX);
        buf.writeDouble(message.posY);
        buf.writeDouble(message.posZ);
    }

    private double getPosX() {
        return posX;
    }

    private double getPosY() {
        return posY;
    }

    private double getPosZ() {
        return posZ;
    }

    /** The tick of the last position taken from a client, per ridden mob (server thread only) */
    private static final java.util.Map<Entity, Long> LAST_POSITION = new java.util.WeakHashMap<>();

    /**
     * Takes the client's position at most once a tick, at most twice the mob's speed away, in a loaded chunk and in free
     * space (the death worm passes through sand). 10 blocks a packet with no rate limit walked riders through walls and
     * hundreds of blocks a tick.
     */
    public boolean isNear(Entity entity) {
        net.minecraft.world.level.Level level = entity.level();
        long now = level.getGameTime();
        Long last = LAST_POSITION.get(entity);
        if (last != null && last == now) {
            return false;
        }
        double dx = this.getPosX() - entity.getX();
        double dy = this.getPosY() - entity.getY();
        double dz = this.getPosZ() - entity.getZ();
        double step = maxStep(entity);
        if (dx * dx + dy * dy + dz * dz > step * step
            || !level.hasChunkAt(net.minecraft.core.BlockPos.containing(this.getPosX(), this.getPosY(), this.getPosZ()))) {
            return false;
        }
        net.minecraft.world.phys.AABB box = entity.getBoundingBox().move(dx, dy, dz);
        if (!(entity instanceof dev.vladeezyy.rampart.server.entity.collision.ICustomCollisions passThrough ? fits(level, passThrough, box) : level.noCollision(entity, box))) {
            return false;
        }
        LAST_POSITION.put(entity, now);
        return true;
    }

    /** Twice the ridden speed in blocks a tick (attribute speed times ~2.2 for ground friction), at least one block */
    private static double maxStep(Entity entity) {
        if (!(entity instanceof net.minecraft.world.entity.LivingEntity living)) {
            return 1;
        }
        double speed = living.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        if (living.getAttributes().hasAttribute(net.minecraft.world.entity.ai.attributes.Attributes.FLYING_SPEED)) {
            speed = Math.max(speed, living.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.FLYING_SPEED));
        }
        return Math.max(1, 2 * 2.2 * speed);
    }

    /** No solid block in the box except the ones the mob passes through (the worm's sand); unloaded chunks count as solid */
    private static boolean fits(net.minecraft.world.level.Level level, dev.vladeezyy.rampart.server.entity.collision.ICustomCollisions mob, net.minecraft.world.phys.AABB box) {
        net.minecraft.core.BlockPos min = net.minecraft.core.BlockPos.containing(box.minX, box.minY, box.minZ);
        net.minecraft.core.BlockPos max = net.minecraft.core.BlockPos.containing(box.maxX, box.maxY, box.maxZ);
        for (net.minecraft.core.BlockPos pos : net.minecraft.core.BlockPos.betweenClosed(min, max)) {
            if (!level.hasChunkAt(pos)) {
                return false;
            }
            net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
            net.minecraft.world.phys.shapes.VoxelShape shape = state.getCollisionShape(level, pos);
            if (!shape.isEmpty() && shape.bounds().move(pos).intersects(box) && !mob.canPassThrough(pos, state, shape)) {
                return false;
            }
        }
        return true;
    }

    public static class Handler {
        public Handler() {
        }

        public static void handle(final MessageDragonControl message, final PacketContext context) {

            context.enqueueWork(() -> {
                Player player = context.player();

                if (context.flow() == net.minecraft.network.protocol.PacketFlow.CLIENTBOUND) {
                    player = IceAndFire.PROXY.getClientSidePlayer();
                }

                if (player != null) {
                    Entity entity = player.level().getEntity(message.dragonId);

                    if (ServerEvents.isRidingOrBeingRiddenBy(entity, player)) {
                        /*
                            For some of these entities the `setPos` is handled in `Entity#move`
                            Doing it here would cause server-side movement checks to fail (resulting in "moved wrongly" messages)
                        */
                        if (entity instanceof EntityDragonBase dragon) {
                            if (dragon.isOwnedBy(player)) {
                                dragon.setControlState(message.controlState);
                            }
                        } else if (entity instanceof EntityHippogryph hippogryph) {
                            if (hippogryph.isOwnedBy(player)) {
                                hippogryph.setControlState(message.controlState);
                            }
                        } else if (entity instanceof EntityHippocampus hippo) {
                            if (hippo.isOwnedBy(player)) {
                                hippo.setControlState(message.controlState);
                            }

                            if (message.isNear(hippo)) {

                                hippo.setPos(message.getPosX(), message.getPosY(), message.getPosZ());

                            }
                        } else if (entity instanceof EntityDeathWorm deathWorm) {
                            deathWorm.setControlState(message.controlState);
                            if (message.isNear(deathWorm)) {
                                deathWorm.setPos(message.getPosX(), message.getPosY(), message.getPosZ());
                            }
                        } else if (entity instanceof EntityAmphithere amphithere) {
                            if (amphithere.isOwnedBy(player)) {
                                amphithere.setControlState(message.controlState);
                            }

                            // TODO :: Is this handled by Entity#move due to recent changes?
                            if (message.isNear(amphithere)) {
                                amphithere.setPos(message.getPosX(), message.getPosY(), message.getPosZ());
                            }
                        }
                    }
                }
            });

        }
    }
}