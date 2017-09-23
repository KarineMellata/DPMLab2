/*
 * OdometryCorrection.java
 */
package ca.mcgill.ecse211.odometerlab;

import lejos.robotics.SampleProvider;
import java.math.*;

public class OdometryCorrection extends Thread {
  private static final long CORRECTION_PERIOD = 10;
  private Odometer odometer;
  private SampleProvider us;
  private float[] color;
  private float calibLight = 0;
  private boolean blackLine;

  // constructor
  public OdometryCorrection(SampleProvider us, float[] color, Odometer odometer) {
    this.odometer = odometer;
    this.us = us;
    this.color = color;
  }

  // run method (required for Thread)
  public void run() {
    long correctionStart, correctionEnd;
    int lightIntensity;
    while (true) {
      correctionStart = System.currentTimeMillis();
      us.fetchSample(color, 0); // acquire data
      lightIntensity = (int) (color[0] * 100); // extract from buffer, cast to int
      
      if (calibLight == 0){
			calibLight = lightIntensity;
		}else{ 
			if (lightIntensity > 10) {
				blackLine = true;
//			}else if(100*Math.abs(currBrightnessLevel - firstBrightnessLevel)/firstBrightnessLevel > significantPercentThreshold){
//				//we have a significant change
//				if(currBrightnessLevel < firstBrightnessLevel){
//					//we've reached a black line!!!
//					reachedBlackLine = true;
//				}
			}else{
				blackLine = false;
			}
			
			//if we've reached a black line, correct the position of the robot.
			if(blackLine == true){
				System.out.print("hello");
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
