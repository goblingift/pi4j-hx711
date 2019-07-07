/* 
 * Copyright (C) 2019 Andre Kessler (https://github.com/goblingift)
 * All rights reserved
 */
package gift.goblin.hx711;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

/**
 * HX711 implementation.
 * @author andre
 */
public class Hx711 {

    private final GpioPinDigitalOutput pinSCK;
    private final GpioPinDigitalInput pinDT;
    private int gain;
    private final int loadCellMaxWeight;
    private double ratedOutput;
    private long tareOffset;
    
    /**
     * Creates an instance of a hx711 sensor.Will tare the scale in constructor.
     * @param pinDT GPIO pin for the DT-wire.
     * @param pinSCK GPIO pin for the SCK-wire.
     * @param loadCellMaxWeight the maximum weight of the load cell in grams.
     * @param ratedOutput rated output of load cell in mv/V. 
     * E.g. value '1.0' or '2.0' are common.
     * @param gainFactor 128-bit is common.
     */
    public Hx711(GpioPinDigitalInput pinDT, GpioPinDigitalOutput pinSCK, int loadCellMaxWeight,
            double ratedOutput, GainFactor gainFactor) {
        this.pinSCK = pinSCK;
        this.pinDT = pinDT;
        this.loadCellMaxWeight = loadCellMaxWeight;
        this.ratedOutput = ratedOutput;

        this.gain = gainFactor.getGain();
        pinSCK.setState(PinState.LOW);
        tareOffset = readValue();
        
        System.out.println("Initialized Hx711-sensor. Tare value is: " + tareOffset);
    }
    
    /**
     * Measures the current load of the scale. (Tare offset will be subtracted)
     * @return the load in grams (g).
     */
    public long measure() {

        double measuredGrams = ((readValue() - tareOffset) * 0.5 * loadCellMaxWeight) / ((ratedOutput / 1000) * 128 * 8388608);
        long measuredGramsRounded = Math.round(measuredGrams);
        
        return measuredGramsRounded;
    }
    
    
    /**
     * Get value from the load cell sensor.
     * @return raw digital value returned by the load cell sensor.
     */
    private long readValue() {
        
        pinSCK.setState(PinState.LOW);
        while (!isReadyForMeasurement()) {
            sleepSafe(1);
        }

        long count = 0;
        for (int i = 0; i < this.gain; i++) {
            pinSCK.setState(PinState.HIGH);
            count = count << 1;
            pinSCK.setState(PinState.LOW);
            if (pinDT.isHigh()) {
                count++;
            }
        }

        pinSCK.setState(PinState.HIGH);
        count = count ^ 0x800000;
        pinSCK.setState(PinState.LOW);
        
        System.out.println("Read value from sensor: " + count);
        
        return count;
    }

    private boolean isReadyForMeasurement() {
        return (pinDT.isLow());
    }

    private void sleepSafe(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            System.err.println(ex.getMessage());
        }
    }
}
