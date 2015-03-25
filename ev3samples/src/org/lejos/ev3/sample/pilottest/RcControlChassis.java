package org.lejos.ev3.sample.pilottest;

import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.DifferentialChassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.localization.PoseProvider;
import lejos.robotics.navigation.Pose;
import lejos.utility.Delay;

public class RcControlChassis {
  public static void main(String[] args) {
  
  EV3IRSensor ir = new EV3IRSensor(SensorPort.S2);
    
    
  Wheel wheel1 = DifferentialChassis.modelWheel(Motor.A,43.2).offset(-72);
  Wheel wheel2 = DifferentialChassis.modelWheel(Motor.D,43.2).offset(72);
  Chassis chassis = new DifferentialChassis(new Wheel[]{wheel1, wheel2}); 
  PoseProvider odometer = chassis.getOdometer();
  double maxLinear = chassis.getMaxLinearSpeed();
  double maxAngular = chassis.getMaxAngularSpeed();
  double linear = 0;
  double angular = 0;
  double step =2;
  
  
  while (!Button.ESCAPE.isDown()) {
    int control = ir.getRemoteCommand(0);
    boolean changed = true;
    switch (control) {
    case 1:
      if (linear<100 -step ) linear += step ;
      if (linear + angular > 100) angular = 100 -linear;
      break;
    case 2:
      if (linear>-100 + step ) linear --;
      if (linear + angular < - 100) angular = -100 -linear;
      break;
    case 3:
      if (angular<100 - step) angular ++;
      if (linear + angular > 100) linear = 100 -angular;
      break;
    case 4:
      if (angular>-100 + step) angular --;
      if (linear + angular < - 100) linear = -100 -angular;
      break;
    case 9:
      linear =0;
      angular =0;
      break;
    case 10 : 
      linear =0;
      break;
    case 11 : 
      angular =0;
      break;
    default:
      changed=false;
        break;
    }
    if (changed) {
    chassis.travel(maxLinear * linear/ 100, maxAngular * angular / 100); 
    }
    Pose pose = odometer.getPose();
    LCD.clear();
    LCD.drawString("xPose: " + pose.getX(), 0, 0);
    LCD.drawString("yPose: " + pose.getY(), 0, 1);
    LCD.drawString("aPose: " + pose.getHeading(), 0, 2);
    LCD.refresh();
    Delay.msDelay(50);
    }
  
  ir.close();
  
  

  }
}
