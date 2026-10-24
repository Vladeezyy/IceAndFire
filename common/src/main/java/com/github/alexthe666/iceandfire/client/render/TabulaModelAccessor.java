package com.github.alexthe666.iceandfire.client.render;

import dev.vladeezyy.rampart.client.model.AdvancedModelBox;
import dev.vladeezyy.rampart.client.model.ITabulaModelAnimator;
import dev.vladeezyy.rampart.client.model.TabulaModel;
import dev.vladeezyy.rampart.client.model.container.TabulaModelContainer;

import java.util.List;

public class TabulaModelAccessor extends TabulaModel {
    public TabulaModelAccessor(TabulaModelContainer container, ITabulaModelAnimator tabulaAnimator) {
        super(container, tabulaAnimator);
    }

    public TabulaModelAccessor(TabulaModelContainer container) {
        super(container);
    }

    public List<AdvancedModelBox> getRootBox() {
        return super.rootBoxes;
    }
}
