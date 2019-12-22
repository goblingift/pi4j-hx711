/*
 * Copyright (C) 2019 Andre Kessler (https://github.com/goblingift)
 * All rights reserved
 */
package gift.goblin.hx711;

/**
 * Enum for gain-factor of hx711 module.
 * For best performance use 128-bit.
 * @author andre
 */
public enum GainFactor {
    
    GAIN_128(24),
    GAIN_64(26),
    // 32-bit is for channel-B of hx711 module
    GAIN_32(25);
    
    private int gain;

    private GainFactor(int gain) {
        this.gain = gain;
    }

    public int getGain() {
        return gain;
    }
    
}
