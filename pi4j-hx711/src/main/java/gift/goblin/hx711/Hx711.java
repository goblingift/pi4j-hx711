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
    }
    
    /**
     * Measures the current load of the scale. (Tare offset will be subtracted)
     * Warning! Be sure that you´ve set the tare value before- otherwise your result 
     * will be faulty.
     * @return the load in grams (g), rounded without decimals.
     */
    public long measure() {

        double measuredKilogram = ((readValue() - tareOffset) * 0.5 * loadCellMaxWeight) / ((ratedOutput / 1000) * 128 * 8388608);
        double measuredGrams = measuredKilogram * 1000;
        long roundedGrams = Math.round(measuredGrams);
        
        return roundedGrams;
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
    
    /**
     * Measures the current value of the load-cell and set this as tare-value.
     * Warning: By executing this method, you will set the empty value for this
     * load cell!
     * @return 
     */
    public long measureAndSetTare() {
        long tareValue = readValue();
        this.tareOffset = tareValue;
        
        return tareValue;
    }
    
    /**
     * Sets the tare-value of the scale to the given value.
     * @param tareValue value you´ll get from the scale when its empty.
     */
    public void setTareValue(long tareValue) {
        this.tareOffset = tareValue;
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
