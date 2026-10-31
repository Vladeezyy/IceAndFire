package com.github.alexthe666.iceandfire.entity.ai;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import com.github.alexthe666.iceandfire.entity.EntityMyrmexBase;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;

import java.util.function.Predicate;

public class MyrmexAIAttackPlayers extends NearestAttackableTargetGoal {

    private final EntityMyrmexBase myrmex;

    @SuppressWarnings("unchecked")
    public MyrmexAIAttackPlayers(EntityMyrmexBase myrmex) {
        super(myrmex, Player.class, 10, true, true, new TargetingConditions.Selector() {

            @Override
            public boolean test(LivingEntity iafTarget, ServerLevel serverLevel) {
                Player entity = (Player) iafTarget;
                return entity != null && (myrmex.getHive() == null
                    || myrmex.getHive().isPlayerReputationLowEnoughToFight(entity.getUUID()));
            }
        });
        this.myrmex = myrmex;
    }

    @Override
    public boolean canUse() {
        return myrmex.shouldHaveNormalAI() && super.canUse();
    }
}
