package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityMyrmexBase;

public class MyrmexQueenAIWander extends MyrmexAIWander {

    public MyrmexQueenAIWander(EntityMyrmexBase myrmex, double speed) {
        super(myrmex, speed);
    }

    @Override
    public boolean canUse() {
        // A queen of a hive stays home (MyrmexAIWanderHiveCenter, MyrmexAIReEnterHive), even under an open sky
        return myrmex.getHive() == null && super.canUse();
    }
}
