/*
 * OdometryCorrection.java
 */
package ca.mcgill.ecse211.odometerlab;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import java.math.*;
import lejos.hardware.Sound;

public class OdometryCorrection extends Thread {
  private static final long CORRECTION_PERIOD = 10;
  private Odometer odometer;
  private SampleProvider us;
  private float[] color;
  private float calibLight = 0;
  private boolean blackLine;
  private double distx = 0;
  private double disty = 0;
  private int counter = 0;

  // constructor
  public OdometryCorrection(SampleProvider us, float[] color, Odometer odometer) {
    this.odometer = odometer;
    this.us = us;
    this.color = color;
  }

  // run method (required for Thread)
  public void run() {
	int blackRead=0;
    long correctionStart, correctionEnd;
    int lightIntensity;
    while (true) {
      correctionStart = System.currentTimeMillis();
      us.fetchSample(color, 0); // acquire data
      lightIntensity = (int) (color[0] * 100); // extract from buffer, cast to int
      
      if (calibLight == 0){
			calibLight = lightIntensity;
		}else{ 
			//The way our correction is if we hit a black line, we know that the distance from the origin
			// must be exactly a multiple of 30.48. Therefore, we divide the actual value by 30.48,
			// and round that number to cut out the error.
			// This ensures the most precise value possible.
			if (lightIntensity < 13 && lightIntensity > 8) { //We hit a black line
				blackLine = true;
				Sound.beep();
					if(odometer.getTheta() >= 358  || odometer.getTheta() <= 3) { // Going at 0 degrees (Increasing Y)
						if(odometer.getY() < 20) { //Setting the Y origin (20 should theoretically be 30.48 but there is a delay)
							odometer.setY(0);
						}
						else {
						disty = odometer.getY()/(30.48); //Corrected value
						odometer.setY(Math.round(disty)*30.48);
						}
					}
					else if(odometer.getTheta() >= 85 && odometer.getTheta() <= 92) { //Turning left (Increasing X)
						if(odometer.getX() < 20) { //Setting the X origin (20 should theoretically be 30.48 but there is a delay)
							odometer.setX(0);
						}
						else {
						distx = odometer.getX()/(30.48); //Corrected value
						odometer.setX(Math.round(distx)*30.48);
						}
					}
					else if(odometer.getTheta() >= 178 && odometer.getTheta() <= 183) { //Turning left again (Decreasing Y)
						disty = odometer.getY()/(30.48);
						odometer.setY(Math.round(disty)*30.48); //Corrected value
					}
					else if(odometer.getTheta() >= 268 && odometer.getTheta() <= 273) { //Turning left again (Decreasing X)
						distx = odometer.getX()/(30.48);
						odometer.setX(Math.round(distx)*30.48); //Corrected value
					}
			}
			else{
				blackLine = false;
			}
		}
      // this ensure the odometry correction occurs only once every period
      correctionEnd = System.currentTimeMillis();
      if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
        try {
          Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
        } catch (InterruptedException e) {
          // there is nothing to be done here because it is not
          // expected that the odometry correction will be
          // interrupted by another thread
        }
      }
    }
  }
}
