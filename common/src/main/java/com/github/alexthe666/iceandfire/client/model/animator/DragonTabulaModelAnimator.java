package com.github.alexthe666.iceandfire.client.model.animator;

import dev.vladeezyy.rampart.animation.Animation;
import dev.vladeezyy.rampart.client.model.AdvancedModelBox;
import dev.vladeezyy.rampart.client.model.ITabulaModelAnimator;
import dev.vladeezyy.rampart.client.model.ModelAnimator;
import dev.vladeezyy.rampart.client.model.TabulaModel;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.client.model.util.EnumDragonPoses;
import com.github.alexthe666.iceandfire.client.model.util.LegArticulator;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

import java.util.Arrays;

public abstract class DragonTabulaModelAnimator<T extends EntityDragonBase> extends IceAndFireTabulaModelAnimator implements ITabulaModelAnimator<T> {

    /** Dragons of one type posed once per frame; more than this in one frame are posed in full every time */
    private static final int MAX_CACHED_POSES = 64;
    /** Floats per cube in a cached pose: rotation and pivot, all that the pose sets */
    private static final int POSE_STRIDE = 6;
    /** Changes whenever a dragon's render state is extracted, so a cached pose never outlives its frame */
    private static int poseEpoch;
    /** Dev check, -Diceandfire.verifyPoseCache=true: every replayed pose is also computed in full and compared */
    private static final boolean VERIFY_POSE_CACHE = Boolean.getBoolean("iceandfire.verifyPoseCache");
    private static int verifyMismatches;

    protected TabulaModel[] walkPoses;
    protected TabulaModel[] flyPoses;
    protected TabulaModel[] swimPoses;
    protected AdvancedModelBox[] neckParts;
    protected AdvancedModelBox[] tailParts;
    protected AdvancedModelBox[] tailPartsWBody;
    protected AdvancedModelBox[] toesPartsL;
    protected AdvancedModelBox[] toesPartsR;
    protected AdvancedModelBox[] clawL;
    protected AdvancedModelBox[] clawR;

    // The posed model's cubes and the same cubes of every pose model, by index: no name lookups per frame
    private TabulaModel resolvedModel;
    private AdvancedModelBox[] cubes;
    private AdvancedModelBox[] baseCubes;
    private final AdvancedModelBox[][] alignedPoses = new AdvancedModelBox[EnumDragonPoses.values().length][];
    private AdvancedModelBox[][] walkCubes;
    private AdvancedModelBox[][] flyCubes;
    private AdvancedModelBox[][] swimCubes;
    private boolean[] wing;
    private boolean[] horn;
    private boolean[] tail;
    private boolean[] finger;
    private boolean[] thigh;
    private AdvancedModelBox bodyUpper, bodyLower, neck1, neck2, jaw;
    private AdvancedModelBox thighL, thighR, legL, legR, armL1, armL2, armR1, armR2;

    private CachedPose[] cachedPoses = new CachedPose[4];
    private int cachedCount;
    private int cachedEpoch;

    public DragonTabulaModelAnimator(TabulaModel baseModel) {
        super(baseModel);
    }

    /** A dragon's render state was extracted (next frame, or a dragon drawn again elsewhere): cached poses are stale */
    public static void newPoseEpoch() {
        poseEpoch++;
    }

    public void init(TabulaModel model) {
        neckParts = new AdvancedModelBox[]{model.getCube("Neck1"), model.getCube("Neck2"), model.getCube("Neck3"), model.getCube("Head")};
        tailParts = new AdvancedModelBox[]{model.getCube("Tail1"), model.getCube("Tail2"), model.getCube("Tail3"), model.getCube("Tail4")};
        tailPartsWBody = new AdvancedModelBox[]{model.getCube("BodyLower"), model.getCube("Tail1"), model.getCube("Tail2"), model.getCube("Tail3"), model.getCube("Tail4")};
        toesPartsL = new AdvancedModelBox[]{model.getCube("ToeL1"), model.getCube("ToeL2"), model.getCube("ToeL3")};
        toesPartsR = new AdvancedModelBox[]{model.getCube("ToeR1"), model.getCube("ToeR2"), model.getCube("ToeR3")};
        clawL = new AdvancedModelBox[]{model.getCube("ClawL")};
        clawR = new AdvancedModelBox[]{model.getCube("ClawR")};

        bodyUpper = model.getCube("BodyUpper");
        bodyLower = model.getCube("BodyLower");
        neck1 = model.getCube("Neck1");
        neck2 = model.getCube("Neck2");
        jaw = model.getCube("Jaw");
        thighL = model.getCube("ThighL");
        thighR = model.getCube("ThighR");
        legL = model.getCube("LegL");
        legR = model.getCube("LegR");
        armL1 = model.getCube("armL1");
        armL2 = model.getCube("armL2");
        armR1 = model.getCube("armR1");
        armR2 = model.getCube("armR2");

        // same order as model.getCubes().values(), which the loops used to walk
        cubes = model.getCubes().values().toArray(new AdvancedModelBox[0]);
        int n = cubes.length;
        wing = new boolean[n];
        horn = new boolean[n];
        tail = new boolean[n];
        finger = new boolean[n];
        thigh = new boolean[n];
        for (int i = 0; i < n; i++) {
            AdvancedModelBox cube = cubes[i];
            wing[i] = isWing(model, cube);
            horn[i] = isHorn(cube);
            tail[i] = cube.boxName.contains("Tail");
            finger[i] = cube.boxName.contains("Finger");
            thigh[i] = cube.boxName.equals("ThighR") || cube.boxName.equals("ThighL");
        }
        baseCubes = align(baseModel);
        walkCubes = align(walkPoses);
        flyCubes = align(flyPoses);
        swimCubes = align(swimPoses);
        Arrays.fill(alignedPoses, null);
        cachedCount = 0;
        resolvedModel = model;
    }

    /** The cubes of {@code pose} matching {@link #cubes} by name; null cubes where the pose lacks one */
    private AdvancedModelBox[] align(TabulaModel pose) {
        if (pose == null) {
            return null;
        }
        AdvancedModelBox[] aligned = new AdvancedModelBox[cubes.length];
        for (int i = 0; i < cubes.length; i++) {
            aligned[i] = pose.getCube(cubes[i].boxName);
        }
        return aligned;
    }

    private AdvancedModelBox[][] align(TabulaModel[] poses) {
        if (poses == null) {
            return null;
        }
        AdvancedModelBox[][] aligned = new AdvancedModelBox[poses.length][];
        for (int i = 0; i < poses.length; i++) {
            aligned[i] = align(poses[i]);
        }
        return aligned;
    }

    /** Resolved on first use, as the old code looked the pose up only when it needed it; a missing pose stays null */
    private AdvancedModelBox[] pose(EnumDragonPoses pose) {
        AdvancedModelBox[] aligned = alignedPoses[pose.ordinal()];
        if (aligned == null) {
            aligned = align(getModel(pose));
            alignedPoses[pose.ordinal()] = aligned;
        }
        return aligned;
    }

    @Override
    public void setRotationAngles(TabulaModel model, T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float rotationYaw, float rotationPitch, float scale) {
        if (model != resolvedModel) {
            init(model);
        }
        // 26.x poses the model again for every submit and layer of the dragon: compute once per frame, replay the rest
        CachedPose cached = findCachedPose(entity, limbSwing, limbSwingAmount, ageInTicks, rotationYaw, rotationPitch);
        if (cached != null) {
            if (VERIFY_POSE_CACHE) {
                poseModel(model, entity, limbSwing, limbSwingAmount, ageInTicks, rotationYaw, rotationPitch, scale);
                cached.verify(cubes, entity);
            }
            cached.restore(cubes);
            return;
        }
        poseModel(model, entity, limbSwing, limbSwingAmount, ageInTicks, rotationYaw, rotationPitch, scale);
        cachePose(entity, limbSwing, limbSwingAmount, ageInTicks, rotationYaw, rotationPitch);
    }

    private CachedPose findCachedPose(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float yaw, float pitch) {
        if (cachedEpoch != poseEpoch) {
            cachedEpoch = poseEpoch;
            cachedCount = 0;
            return null;
        }
        int id = entity.getId();
        Object uuid = entity.getUUID();
        for (int i = 0; i < cachedCount; i++) {
            CachedPose cached = cachedPoses[i];
            if (cached.entityId == id && cached.uuid == uuid && cached.limbSwing == limbSwing && cached.limbSwingAmount == limbSwingAmount
                    && cached.ageInTicks == ageInTicks && cached.yaw == yaw && cached.pitch == pitch) {
                return cached;
            }
        }
        return null;
    }

    private void cachePose(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float yaw, float pitch) {
        if (cachedCount == cachedPoses.length) {
            if (cachedCount >= MAX_CACHED_POSES) {
                return;
            }
            cachedPoses = Arrays.copyOf(cachedPoses, Math.min(cachedCount * 2, MAX_CACHED_POSES));
        }
        CachedPose cached = cachedPoses[cachedCount];
        if (cached == null || cached.pose.length != cubes.length * POSE_STRIDE) {
            cached = cachedPoses[cachedCount] = new CachedPose(cubes.length * POSE_STRIDE);
        }
        // the id and the UUID object, not the entity: no reference to the level after leaving it
        cached.entityId = entity.getId();
        cached.uuid = entity.getUUID();
        cached.limbSwing = limbSwing;
        cached.limbSwingAmount = limbSwingAmount;
        cached.ageInTicks = ageInTicks;
        cached.yaw = yaw;
        cached.pitch = pitch;
        cached.store(cubes);
        cachedCount++;
    }

    private void poseModel(TabulaModel model, T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float rotationYaw, float rotationPitch, float scale) {
        // model.resetToDefaultPose() without copying the cube list
        for (AdvancedModelBox cube : cubes) {
            cube.resetToDefaultPose();
        }
        animate(model, entity, limbSwing, limbSwingAmount, ageInTicks, rotationYaw, rotationPitch, scale);

        boolean walking = !entity.isHovering() && !entity.isFlying() && entity.hoverProgress <= 0 && entity.flyProgress <= 0;
        boolean swimming = entity.isInWater() && entity.swimProgress > 0;

        int currentIndex = walking ? (entity.walkCycle / 10) : (entity.flightCycle / 10);
        if (swimming) {
            currentIndex = entity.swimCycle / 10;
        }
        int prevIndex = currentIndex - 1;
        if (prevIndex < 0) {
            prevIndex = swimming ? 4 : walking ? 3 : 5;
        }

        AdvancedModelBox[] currentPosition = swimming ? swimCubes[currentIndex] : walking ? walkCubes[currentIndex] : flyCubes[currentIndex];
        AdvancedModelBox[] prevPosition = swimming ? swimCubes[prevIndex] : walking ? walkCubes[prevIndex] : flyCubes[prevIndex];
        float delta = ((walking ? entity.walkCycle : entity.flightCycle) / 10.0F) % 1.0F;
        if (swimming) {
            delta = ((entity.swimCycle) / 10.0F) % 1.0F;
        }
        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float deltaTicks = delta + (partialTick / 10.0F);
        if (delta == 0) {
            deltaTicks = 0;
        }

        float speed_walk = 0.2F;
        float speed_idle = entity.isSleeping() ? 0.025F : 0.05F;
        float speed_fly = 0.2F;
        float degree_walk = 0.5F;
        float degree_idle = entity.isSleeping() ? 0.25F : 0.5F;
        float degree_fly = 0.5F;

        if (entity.isModelDead()) {
            poseDead(entity, partialTick);
            return;
        }

        if (entity.isAiDisabled())
            return;

        poseAllCubes(entity, limbSwingAmount, walking, currentPosition, prevPosition, partialTick, deltaTicks);
        if (entity.getAnimation() != EntityDragonBase.ANIMATION_SHAKEPREY || entity.getAnimation() != EntityDragonBase.ANIMATION_ROAR) {
            model.faceTarget(rotationYaw, rotationPitch, 2, neckParts);
        }
        if (!walking) {
            model.bob(bodyUpper, -speed_fly, degree_fly * 5, false, ageInTicks, 1);
            model.walk(bodyUpper, -speed_fly, degree_fly * 0.1F, false, 0, 0, ageInTicks, 1);
            model.chainWave(tailPartsWBody, speed_fly, degree_fly * -0.1F, 0, ageInTicks, 1);
            model.chainWave(neckParts, speed_fly, degree_fly * 0.2F, -4, ageInTicks, 1);
            model.chainWave(toesPartsL, speed_fly, degree_fly * 0.2F, -2, ageInTicks, 1);
            model.chainWave(toesPartsR, speed_fly, degree_fly * 0.2F, -2, ageInTicks, 1);
            model.walk(thighR, -speed_fly, degree_fly * 0.1F, false, 0, 0, ageInTicks, 1);
            model.walk(thighL, -speed_fly, degree_fly * 0.1F, true, 0, 0, ageInTicks, 1);
        } else {
            model.bob(bodyUpper, speed_walk * 2, degree_walk * 1.7F, false, limbSwing, limbSwingAmount);
            model.bob(thighR, speed_walk, degree_walk * 1.7F, false, limbSwing, limbSwingAmount);
            model.bob(thighL, speed_walk, degree_walk * 1.7F, false, limbSwing, limbSwingAmount);
            model.chainSwing(tailParts, speed_walk, degree_walk * 0.25F, -2, limbSwing, limbSwingAmount);
            model.chainWave(tailParts, speed_walk, degree_walk * 0.15F, 2, limbSwing, limbSwingAmount);
            model.chainSwing(neckParts, speed_walk, degree_walk * 0.15F, 2, limbSwing, limbSwingAmount);
            model.chainWave(neckParts, speed_walk, degree_walk * 0.05F, -2, limbSwing, limbSwingAmount);
            model.chainSwing(tailParts, speed_idle, degree_idle * 0.25F, -2, ageInTicks, 1);
            model.chainWave(tailParts, speed_idle, degree_idle * 0.15F, -2, ageInTicks, 1);
            model.chainWave(neckParts, speed_idle, degree_idle * -0.15F, -3, ageInTicks, 1);
            model.walk(neck1, speed_idle, degree_idle * 0.05F, false, 0, 0, ageInTicks, 1);
        }
        model.bob(bodyUpper, speed_idle, degree_idle * 1.3F, false, ageInTicks, 1);
        model.bob(thighR, speed_idle, -degree_idle * 1.3F, false, ageInTicks, 1);
        model.bob(thighL, speed_idle, -degree_idle * 1.3F, false, ageInTicks, 1);
        model.bob(armR1, speed_idle, -degree_idle * 1.3F, false, ageInTicks, 1);
        model.bob(armL1, speed_idle, -degree_idle * 1.3F, false, ageInTicks, 1);

        if (entity.isActuallyBreathingFire()) {
            float speed_shake = 0.7F;
            float degree_shake = 0.1F;
            model.chainFlap(neckParts, speed_shake, degree_shake, 2, ageInTicks, 1);
            model.chainSwing(neckParts, speed_shake * 0.65F, degree_shake * 0.1F, 1, ageInTicks, 1);
        }


        if (entity.turn_buffer != null && !entity.isVehicle() && !entity.isPassenger() && entity.isBreathingFire()) {
            entity.turn_buffer.applyChainSwingBuffer(neckParts);
        }
        if (entity.tail_buffer != null && !entity.isPassenger()) {
            entity.tail_buffer.applyChainSwingBuffer(tailPartsWBody);
        }
        if (entity.roll_buffer != null && entity.pitch_buffer_body != null && entity.pitch_buffer != null) {
            if (entity.flyProgress > 0 || entity.hoverProgress > 0) {
                entity.roll_buffer.applyChainFlapBuffer(bodyUpper);
                entity.pitch_buffer_body.applyChainWaveBuffer(bodyUpper);
                entity.pitch_buffer.applyChainWaveBufferReverse(tailPartsWBody);
            }
        }
        if (entity.getBbWidth() >= 2 && entity.flyProgress == 0 && entity.hoverProgress == 0) {
            LegArticulator.articulateQuadruped(entity, entity.legSolver, bodyUpper, bodyLower, neck1,
                    thighL, legL, toesPartsL,
                    thighR, legR, toesPartsR,
                    armL1, armL2, clawL,
                    armR1, armR2, clawR,
                    1.0F, 0.5F, 0.5F, -0.15F, -0.15F, 0F,
                    partialTick
            );
        }
    }

    /** Per cube, in the old order: gender, walk cycle, the state poses, breath, flight cycle; entity values read once */
    private void poseAllCubes(T entity, float limbSwingAmount, boolean walking, AdvancedModelBox[] currentPosition, AdvancedModelBox[] prevPosition, float partialTick, float deltaTicks) {
        boolean female = !entity.isMale();
        AdvancedModelBox[] maleCubes = female ? pose(EnumDragonPoses.MALE) : null;
        AdvancedModelBox[] femaleCubes = female ? pose(EnumDragonPoses.FEMALE) : null;

        boolean walkCycle = walking && entity.flyProgress <= 0.0F && entity.hoverProgress <= 0.0F && entity.modelDeadProgress <= 0.0F;
        AdvancedModelBox[] groundCubes = walkCycle ? pose(EnumDragonPoses.GROUND_POSE) : null;
        Animation animation = entity.getAnimation();
        boolean wingAnimation = animation == EntityDragonBase.ANIMATION_WINGBLAST || animation == EntityDragonBase.ANIMATION_EPIC_ROAR;

        AdvancedModelBox[] sleepCubes = null;
        float sleep = 0;
        if (entity.sleepProgress > 0.0F) {
            sleepCubes = pose(EnumDragonPoses.SLEEPING_POSE);
            sleep = Mth.lerp(partialTick, entity.prevAnimationProgresses[1], entity.sleepProgress);
        }
        AdvancedModelBox[] hoverCubes = null;
        float hover = 0;
        if (entity.hoverProgress > 0.0F) {
            hoverCubes = pose(EnumDragonPoses.HOVERING_POSE);
            hover = Mth.lerp(partialTick, entity.prevAnimationProgresses[2], entity.hoverProgress);
        }
        AdvancedModelBox[] flyCubesPose = null;
        float fly = 0;
        if (entity.flyProgress > 0.0F) {
            flyCubesPose = pose(EnumDragonPoses.FLYING_POSE);
            fly = Mth.lerp(partialTick, entity.prevAnimationProgresses[3], entity.flyProgress) - (Mth.lerp(partialTick, entity.prevDiveProgress, entity.diveProgress)) * 2;
        }
        AdvancedModelBox[] sitCubes = null;
        float sit = 0;
        if (entity.sitProgress > 0.0F && !entity.isPassenger()) {
            sitCubes = pose(EnumDragonPoses.SITTING_POSE);
            sit = Mth.lerp(partialTick, entity.prevAnimationProgresses[0], entity.sitProgress);
        }
        AdvancedModelBox[] rideCubes = null;
        float ride = 0;
        if (entity.ridingProgress > 0.0F) {
            rideCubes = pose(EnumDragonPoses.SIT_ON_PLAYER_POSE);
            ride = Mth.lerp(partialTick, entity.prevAnimationProgresses[5], entity.ridingProgress);
        }
        AdvancedModelBox[] tackleCubes = null;
        AdvancedModelBox[] tackleFlyCubes = null;
        float tackle = 0;
        if (entity.tackleProgress > 0.0F) {
            tackleCubes = pose(EnumDragonPoses.TACKLE);
            tackleFlyCubes = pose(EnumDragonPoses.FLYING_POSE);
            tackle = Mth.lerp(partialTick, entity.prevAnimationProgresses[6], entity.tackleProgress);
        }
        AdvancedModelBox[] diveCubes = null;
        float dive = 0;
        if (entity.diveProgress > 0.0F) {
            diveCubes = pose(EnumDragonPoses.DIVING_POSE);
            dive = Mth.lerp(partialTick, entity.prevDiveProgress, entity.diveProgress);
        }
        AdvancedModelBox[] streamCubes = null;
        AdvancedModelBox[] chargeCubes = null;
        float charge = 0;
        float stream = 0;
        if (entity.fireBreathProgress > 0.0F) {
            streamCubes = pose(EnumDragonPoses.STREAM_BREATH);
            if (entity.prevFireBreathProgress <= entity.fireBreathProgress) {
                chargeCubes = pose(EnumDragonPoses.BLAST_CHARGE3);
                charge = Mth.clamp(Mth.lerp(partialTick, entity.prevFireBreathProgress, entity.fireBreathProgress), 0, 5);
            }
            stream = Mth.clamp(Mth.lerp(partialTick, entity.prevFireBreathProgress, entity.fireBreathProgress) - 5, 0, 5);
        }
        AdvancedModelBox[] flightCubes = walking ? null : pose(EnumDragonPoses.FLYING_POSE);

        for (int i = 0; i < cubes.length; i++) {
            AdvancedModelBox cube = cubes[i];
            if (female) {
                AdvancedModelBox femaleModelCube = femaleCubes[i];
                AdvancedModelBox maleModelCube = maleCubes[i];
                if (maleModelCube != null && femaleModelCube != null) {
                    float x = femaleModelCube.rotateAngleX;
                    float y = femaleModelCube.rotateAngleY;
                    float z = femaleModelCube.rotateAngleZ;
                    if (x != maleModelCube.rotateAngleX || y != maleModelCube.rotateAngleY || z != maleModelCube.rotateAngleZ) {
                        this.setRotateAngle(cube, 1F, x, y, z);
                    }
                }
            }
            if (walkCycle) {
                AdvancedModelBox prevPositionCube = prevPosition[i];
                AdvancedModelBox currPositionCube = currentPosition[i];

                // the rest of this cube is skipped, as before
                if (prevPositionCube == null || currPositionCube == null)
                    continue;

                float prevX = prevPositionCube.rotateAngleX;
                float prevY = prevPositionCube.rotateAngleY;
                float prevZ = prevPositionCube.rotateAngleZ;
                float x = currPositionCube.rotateAngleX;
                float y = currPositionCube.rotateAngleY;
                float z = currPositionCube.rotateAngleZ;
                if (horn[i] || wing[i] && wingAnimation) {
                    AdvancedModelBox walkPart = groundCubes[i];
                    this.addToRotateAngle(cube, limbSwingAmount, walkPart.rotateAngleX, walkPart.rotateAngleY, walkPart.rotateAngleZ);
                } else {
                    this.addToRotateAngle(cube, limbSwingAmount, prevX + deltaTicks * distance(prevX, x), prevY + deltaTicks * distance(prevY, y), prevZ + deltaTicks * distance(prevZ, z));
                }
            }
            if (sleepCubes != null) {
                if (!isRotationEqual(cube, sleepCubes[i])) {
                    transitionTo(cube, sleepCubes[i], sleep, 20, false);
                }
            }
            if (hoverCubes != null) {
                if (!isRotationEqual(cube, hoverCubes[i]) && !wing[i] && !tail[i]) {
                    transitionTo(cube, hoverCubes[i], hover, 20, false);
                }
            }
            if (flyCubesPose != null) {
                if (!isRotationEqual(cube, flyCubesPose[i])) {
                    transitionTo(cube, flyCubesPose[i], fly, 20, false);
                }
            }
            if (sitCubes != null) {
                if (!isRotationEqual(cube, sitCubes[i])) {
                    transitionTo(cube, sitCubes[i], sit, 20, false);
                }
            }
            if (rideCubes != null) {
                if (!horn[i] && !isRotationEqual(cube, rideCubes[i])) {
                    transitionTo(cube, rideCubes[i], ride, 20, false);
                    if (cube == bodyUpper) {
                        cube.offsetZ += ((-12F - cube.offsetZ) / 20) * ride;
                    }
                }
            }
            if (tackleCubes != null) {
                if (!isRotationEqual(tackleCubes[i], tackleFlyCubes[i]) && !wing[i]) {
                    transitionTo(cube, tackleCubes[i], tackle, 5, false);
                }
            }
            if (diveCubes != null) {
                if (!isRotationEqual(cube, diveCubes[i])) {
                    transitionTo(cube, diveCubes[i], dive, 10, false);
                }
            }
            if (streamCubes != null) {
                if (!isRotationEqual(cube, streamCubes[i]) && !wing[i] && !finger[i]) {
                    if (chargeCubes != null) {
                        transitionTo(cube, chargeCubes[i], charge, 5, false);
                    }
                    transitionTo(cube, streamCubes[i], stream, 5, false);
                }
            }
            if (flightCubes != null) {
                AdvancedModelBox flightPart = flightCubes[i];
                AdvancedModelBox prevPositionCube = prevPosition[i];
                AdvancedModelBox currPositionCube = currentPosition[i];
                float prevX = prevPositionCube.rotateAngleX;
                float prevY = prevPositionCube.rotateAngleY;
                float prevZ = prevPositionCube.rotateAngleZ;
                float x = currPositionCube.rotateAngleX;
                float y = currPositionCube.rotateAngleY;
                float z = currPositionCube.rotateAngleZ;
                // Skip only the parts the flight cycle does not move (both key frames are the flying pose). The original
                // skipped a segment whenever its END frame was the flying pose: the lightning dragon's head (flight2 =
                // flying pose, 28.7°) jumped to the still pose once per wing beat (#4768)
                boolean prevIsPose = prevX == flightPart.rotateAngleX && prevY == flightPart.rotateAngleY && prevZ == flightPart.rotateAngleZ;
                boolean currIsPose = x == flightPart.rotateAngleX && y == flightPart.rotateAngleY && z == flightPart.rotateAngleZ;
                if (!prevIsPose || !currIsPose) {
                    this.setRotateAngle(cube, 1F, prevX + deltaTicks * distance(prevX, x), prevY + deltaTicks * distance(prevY, y), prevZ + deltaTicks * distance(prevZ, z));
                }
            }
        }
    }

    private void poseDead(T entity, float partialTick) {
        if (entity.modelDeadProgress > 0.0F) {
            // TODO: Figure out what's up with custom poses
            // DON'T use this in it's current state since it heavily effects render performance due to the fact that
            // custom poses aren't being used right now
            // TabulaModel customPose = customPose(entity);
            AdvancedModelBox[] pose = pose(EnumDragonPoses.DEAD);
            float progress = entity.prevModelDeadProgress + (entity.modelDeadProgress - entity.prevModelDeadProgress) * Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
            //Ugly hack to make sure ice dragon models are touching the ground when dead
            boolean iceDragon = this instanceof IceDragonTabulaModelAnimator;
            for (int i = 0; i < cubes.length; i++) {
                AdvancedModelBox cube = cubes[i];
                if (!isRotationEqual(cube, pose[i])) {
                    transitionTo(cube, pose[i], progress, 20, thigh[i]);
                }
                if (iceDragon && cube == bodyUpper) {
                    cube.rotationPointY += 0.35F * Mth.lerp(partialTick, entity.prevModelDeadProgress, entity.modelDeadProgress);
                }
            }
        }
    }

    protected boolean isWing(TabulaModel model, AdvancedModelBox modelRenderer) {
        return model.getCube("armL1") == modelRenderer || model.getCube("armR1") == modelRenderer || model.getCube("armL1").childModels.contains(modelRenderer) || model.getCube("armR1").childModels.contains(modelRenderer);
    }

    protected TabulaModel customPose(T entity) {
        try {

            return getModel(EnumDragonPoses.valueOf(entity.getCustomPose()));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }


    protected boolean isHorn(AdvancedModelBox modelRenderer) {
        return modelRenderer.boxName.contains("Horn");
    }

    protected abstract TabulaModel getModel(EnumDragonPoses pose);

    /** {@link #moveToPose(TabulaModel, TabulaModel)} by index */
    private void moveToPose(TabulaModel model, AdvancedModelBox[] pose) {
        ModelAnimator animator = model.llibAnimator;
        for (int i = 0; i < cubes.length; i++) {
            AdvancedModelBox cube = cubes[i];
            AdvancedModelBox cubeTo = pose[i];
            AdvancedModelBox base = baseCubes[i];
            if (!isRotationEqual(base, cubeTo)) {
                float toX = cubeTo.rotateAngleX;
                float toY = cubeTo.rotateAngleY;
                float toZ = cubeTo.rotateAngleZ;
                animator.rotate(cube, distance(cube.rotateAngleX, toX), distance(cube.rotateAngleY, toY), distance(cube.rotateAngleZ, toZ));
            }
            if (!isPositionEqual(base, cubeTo)) {
                float toX = cubeTo.rotationPointX;
                float toY = cubeTo.rotationPointY;
                float toZ = cubeTo.rotationPointZ;
                animator.move(cube, toX - cube.rotationPointX, toY - cube.rotationPointY, toZ - cube.rotationPointZ);
            }
        }
    }

    protected void animate(TabulaModel model, T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float rotationYaw, float rotationPitch, float scale) {
        AdvancedModelBox modelCubeJaw = jaw;
        AdvancedModelBox modelCubeBodyUpper = bodyUpper;
        model.llibAnimator.update(entity);
        //Firecharge
        if (model.llibAnimator.setAnimation(T.ANIMATION_FIRECHARGE)) {
            model.llibAnimator.startKeyframe(10);
            moveToPose(model, pose(EnumDragonPoses.BLAST_CHARGE1));
            model.llibAnimator.endKeyframe();
            model.llibAnimator.startKeyframe(10);
            moveToPose(model, pose(EnumDragonPoses.BLAST_CHARGE2));
            model.llibAnimator.endKeyframe();
            model.llibAnimator.startKeyframe(5);
            moveToPose(model, pose(EnumDragonPoses.BLAST_CHARGE3));
            model.llibAnimator.endKeyframe();
            model.llibAnimator.resetKeyframe(5);
        }
        //Speak
        if (model.llibAnimator.setAnimation(T.ANIMATION_SPEAK)) {
            model.llibAnimator.startKeyframe(5);
            this.rotate(model.llibAnimator, modelCubeJaw, 18, 0, 0);
            model.llibAnimator.move(modelCubeJaw, 0, 0, 0.2F);
            model.llibAnimator.endKeyframe();
            model.llibAnimator.setStaticKeyframe(5);
            model.llibAnimator.startKeyframe(5);
            this.rotate(model.llibAnimator, modelCubeJaw, 18, 0, 0);
            model.llibAnimator.move(modelCubeJaw, 0, 0, 0.2F);
            model.llibAnimator.endKeyframe();
            model.llibAnimator.resetKeyframe(5);
        }
        //Bite
        if (model.llibAnimator.setAnimation(T.ANIMATION_BITE)) {
            model.llibAnimator.startKeyframe(10);
            moveToPose(model, pose(EnumDragonPoses.BITE1));
            model.llibAnimator.endKeyframe();
            model.llibAnimator.startKeyframe(5);
            moveToPose(model, pose(EnumDragonPoses.BITE2));
            model.llibAnimator.endKeyframe();
            model.llibAnimator.startKeyframe(5);
            moveToPose(model, pose(EnumDragonPoses.BITE3));
            model.llibAnimator.endKeyframe();
            model.llibAnimator.resetKeyframe(10);
        }
        //Shakeprey
        if (model.llibAnimator.setAnimation(T.ANIMATION_SHAKEPREY)) {
            model.llibAnimator.startKeyframe(15);
            moveToPose(model, pose(EnumDragonPoses.GRAB1));
            model.llibAnimator.endKeyframe();
            model.llibAnimator.startKeyframe(10);
            moveToPose(model, pose(EnumDragonPoses.GRAB2));
            model.llibAnimator.endKeyframe();
            model.llibAnimator.startKeyframe(10);
            moveToPose(model, pose(EnumDragonPoses.GRAB_SHAKE1));
            model.llibAnimator.endKeyframe();
            model.llibAnimator.startKeyframe(10);
            moveToPose(model, pose(EnumDragonPoses.GRAB_SHAKE2));
            model.llibAnimator.endKeyframe();
            model.llibAnimator.startKeyframe(10);
            moveToPose(model, pose(EnumDragonPoses.GRAB_SHAKE3));
            model.llibAnimator.endKeyframe();
            model.llibAnimator.resetKeyframe(10);
        }
        //Tailwhack
        if (model.llibAnimator.setAnimation(T.ANIMATION_TAILWHACK)) {
            model.llibAnimator.startKeyframe(10);
            moveToPose(model, pose(EnumDragonPoses.TAIL_WHIP1));
            model.llibAnimator.endKeyframe();
            model.llibAnimator.startKeyframe(10);
            moveToPose(model, pose(EnumDragonPoses.TAIL_WHIP2));
            model.llibAnimator.endKeyframe();
            model.llibAnimator.startKeyframe(10);
            moveToPose(model, pose(EnumDragonPoses.TAIL_WHIP3));
            model.llibAnimator.endKeyframe();
            model.llibAnimator.resetKeyframe(10);
        }
        //Wingblast
        if (model.llibAnimator.setAnimation(T.ANIMATION_WINGBLAST)) {
            model.llibAnimator.startKeyframe(5);
            moveToPose(model, pose(EnumDragonPoses.WING_BLAST1));
            model.llibAnimator.move(modelCubeBodyUpper, 0, 0, 0);
            model.llibAnimator.endKeyframe();
            model.llibAnimator.startKeyframe(5);
            moveToPose(model, pose(EnumDragonPoses.WING_BLAST2));
            model.llibAnimator.move(modelCubeBodyUpper, 0, -2F, 0);
            model.llibAnimator.endKeyframe();
            model.llibAnimator.startKeyframe(5);
            moveToPose(model, pose(EnumDragonPoses.WING_BLAST3));
            model.llibAnimator.move(modelCubeBodyUpper, 0, -4F, 0);
            model.llibAnimator.endKeyframe();
            model.llibAnimator.startKeyframe(5);
            moveToPose(model, pose(EnumDragonPoses.WING_BLAST4));
            model.llibAnimator.move(modelCubeBodyUpper, 0, -4F, 0);
            model.llibAnimator.endKeyframe();
            model.llibAnimator.startKeyframe(5);
            moveToPose(model, pose(EnumDragonPoses.WING_BLAST5));
            model.llibAnimator.move(modelCubeBodyUpper, 0, -4F, 0);
            model.llibAnimator.endKeyframe();
            model.llibAnimator.startKeyframe(5);
            moveToPose(model, pose(EnumDragonPoses.WING_BLAST6));
            model.llibAnimator.move(modelCubeBodyUpper, 0, -4F, 0);
            model.llibAnimator.endKeyframe();
            model.llibAnimator.startKeyframe(5);
            moveToPose(model, pose(EnumDragonPoses.WING_BLAST7));
            model.llibAnimator.move(modelCubeBodyUpper, 0, -2F, 0);
            model.llibAnimator.endKeyframe();
            model.llibAnimator.resetKeyframe(10);
        }
        //Roar
        if (model.llibAnimator.setAnimation(T.ANIMATION_ROAR)) {
            model.llibAnimator.startKeyframe(10);
            moveToPose(model, pose(EnumDragonPoses.ROAR1));
            model.llibAnimator.endKeyframe();
            model.llibAnimator.startKeyframe(10);
            moveToPose(model, pose(EnumDragonPoses.ROAR2));
            model.llibAnimator.endKeyframe();
            model.llibAnimator.startKeyframe(10);
            moveToPose(model, pose(EnumDragonPoses.ROAR3));
            model.llibAnimator.endKeyframe();
            model.llibAnimator.resetKeyframe(10);
        }
        //Epicroar
        if (model.llibAnimator.setAnimation(T.ANIMATION_EPIC_ROAR)) {
            model.llibAnimator.startKeyframe(10);
            moveToPose(model, pose(EnumDragonPoses.EPIC_ROAR1));
            model.llibAnimator.rotate(modelCubeBodyUpper, -0.1F, 0, 0);
            model.llibAnimator.endKeyframe();
            model.llibAnimator.startKeyframe(10);
            moveToPose(model, pose(EnumDragonPoses.EPIC_ROAR2));
            model.llibAnimator.rotate(modelCubeBodyUpper, -0.2F, 0, 0);
            model.llibAnimator.endKeyframe();
            model.llibAnimator.startKeyframe(10);
            moveToPose(model, pose(EnumDragonPoses.EPIC_ROAR3));
            model.llibAnimator.rotate(modelCubeBodyUpper, -0.2F, 0, 0);
            model.llibAnimator.endKeyframe();
            model.llibAnimator.startKeyframe(10);
            moveToPose(model, pose(EnumDragonPoses.EPIC_ROAR2));
            model.llibAnimator.rotate(modelCubeBodyUpper, -0.2F, 0, 0);
            model.llibAnimator.endKeyframe();
            model.llibAnimator.startKeyframe(10);
            moveToPose(model, pose(EnumDragonPoses.EPIC_ROAR3));
            model.llibAnimator.rotate(modelCubeBodyUpper, -0.1F, 0, 0);
            model.llibAnimator.endKeyframe();
            model.llibAnimator.resetKeyframe(10);
        }
        // EATING
        if (model.llibAnimator.setAnimation(T.ANIMATION_EAT)) {
            model.llibAnimator.startKeyframe(5);
            this.rotate(model.llibAnimator, neck1, 18, 0, 0);
            this.rotate(model.llibAnimator, neck2, 18, 0, 0);
            model.llibAnimator.endKeyframe();
            //CODE from speak
            model.llibAnimator.startKeyframe(5);
            this.rotate(model.llibAnimator, modelCubeJaw, 18, 0, 0);
            model.llibAnimator.move(modelCubeJaw, 0, 0, 0.2F);
            model.llibAnimator.endKeyframe();
            model.llibAnimator.setStaticKeyframe(5);
            model.llibAnimator.startKeyframe(5);
            this.rotate(model.llibAnimator, modelCubeJaw, 18, 0, 0);
            model.llibAnimator.move(modelCubeJaw, 0, 0, 0.2F);
            model.llibAnimator.endKeyframe();
            model.llibAnimator.startKeyframe(5);
            this.rotate(model.llibAnimator, neck1, -18, 0, 0);
            this.rotate(model.llibAnimator, neck2, -18, 0, 0);
            model.llibAnimator.endKeyframe();
        }
    }

    /** One dragon's pose in the current frame */
    private static final class CachedPose {
        int entityId;
        Object uuid;
        float limbSwing;
        float limbSwingAmount;
        float ageInTicks;
        float yaw;
        float pitch;
        final float[] pose;

        CachedPose(int size) {
            this.pose = new float[size];
        }

        void store(AdvancedModelBox[] cubes) {
            float[] p = this.pose;
            int k = 0;
            for (AdvancedModelBox cube : cubes) {
                p[k++] = cube.rotateAngleX;
                p[k++] = cube.rotateAngleY;
                p[k++] = cube.rotateAngleZ;
                p[k++] = cube.rotationPointX;
                p[k++] = cube.rotationPointY;
                p[k++] = cube.rotationPointZ;
            }
        }

        /** VERIFY_POSE_CACHE: the cubes hold a pose computed in full; log the first cubes that differ from the cached one */
        void verify(AdvancedModelBox[] cubes, Entity entity) {
            float[] p = this.pose;
            int k = 0;
            for (AdvancedModelBox cube : cubes) {
                if (p[k] != cube.rotateAngleX || p[k + 1] != cube.rotateAngleY || p[k + 2] != cube.rotateAngleZ
                        || p[k + 3] != cube.rotationPointX || p[k + 4] != cube.rotationPointY || p[k + 5] != cube.rotationPointZ) {
                    if (verifyMismatches++ < 50) {
                        IceAndFire.LOGGER.warn("Dragon pose cache mismatch: {} cube {}", entity, cube.boxName);
                    }
                }
                k += POSE_STRIDE;
            }
        }

        void restore(AdvancedModelBox[] cubes) {
            float[] p = this.pose;
            int k = 0;
            for (AdvancedModelBox cube : cubes) {
                cube.rotateAngleX = p[k++];
                cube.rotateAngleY = p[k++];
                cube.rotateAngleZ = p[k++];
                cube.rotationPointX = p[k++];
                cube.rotationPointY = p[k++];
                cube.rotationPointZ = p[k++];
            }
        }
    }
}
