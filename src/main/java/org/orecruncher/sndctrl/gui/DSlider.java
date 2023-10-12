package org.orecruncher.sndctrl.gui;

import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ForgeSlider;

public class DSlider extends ForgeSlider {
    private final int index;
    private QuickVolumeScreen parent;

    public DSlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue, double currentValue, double stepSize, int precision, boolean drawString) {
        super(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, stepSize, precision, drawString);
        index=0;
    }

    public DSlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue, double currentValue, boolean drawString, int index) {
        super(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, drawString);
        this.index = index;
    }

    public void setParent(QuickVolumeScreen parent) {
        this.parent = parent;
    }

    @Override
    protected void applyValue() {
        // Need to identify the ISoundCategory associated with the slider.
        // Cache the value so we can set all at once
        float v = this.getValueInt() / 100F;
        parent.categoryValues.set(index, v);
    }
}
