package lejos.hardware.motor;

import lejos.hardware.Device;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.port.TachoMotorPort;
import lejos.internal.ev3.EV3MotorRegulator;
import lejos.internal.ev3.EV3MotorRegulatorFP;
import lejos.internal.ev3.EV3MotorRegulatorKM;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.RegulatedMotorListener;
import lejos.utility.Delay;

/**
 * Abstraction for a NXT motor.
 * The basic control methods are:
 *  <code>forward, backward, reverseDirection, stop</code>
 * and <code>flt</code>. To set each motor's velocity, use {@link #setSpeed(int)
 * <code>setSpeed  </code> }.
 * The maximum velocity of the motor is limited by the battery voltage and load.
 * With no load, the maximum degrees per second is about 100 times the voltage.  <br>
 * The velocity is regulated by comparing the tacho count with velocity times elapsed
 * time, and adjusting motor power to keep these closely matched. Changes in velocity
 * will be made at the rate specified via the
 * <code> setAcceleration(int acceleration)</code> method.
 * The methods <code>rotate(int angle) </code> and <code>rotateTo(int ange)</code>
 * use the tachometer to control the position at which the motor stops, usually within 1 degree
 * or 2.<br>
 *  <br> <b> Listeners.</b>  An object implementing the {@link lejos.robotics.RegulatedMotorListener
 * <code> RegulatedMotorListener </code> } interface  may register with this class.
 * It will be informed each time the motor starts or stops.
 * <br> <b>Stall detection</b> If a stall is detected or if for some other reason
 * the speed regulation fails, the motor will stop, and
 * <code>isStalled()</code >  returns <b>true</b>.
 * <br>Motors will hold thier position when stopped. If this is not what you require use
 * the flt() method instead of stop().
 * <br>
 * <p>
 * Example:<p>
 * <code><pre>
 *   Motor.A.setSpeed(720);// 2 RPM
 *   Motor.C.setSpeed(720);
 *   Motor.A.forward();
 *   Motor.C.forward();
 *   Delay.msDelay(1000);
 *   Motor.A.stop();
 *   Motor.C.stop();
 *   Motor.A.rotateTo( 360);
 *   Motor.A.rotate(-720,true);
 *   while(Motor.A.isMoving()Thread.yield();
 *   int angle = Motor.A.getTachoCount(); // should be -360
 *   LCD.drawInt(angle,0,0);
 * </pre></code>
 * TODO: Fix the name
 * @author Roger Glassey/Andy Shaw
 */
public class LargeRegulatedMotor extends Device implements RegulatedMotor
{

    protected static final int NO_LIMIT = 0x7fffffff;
    protected final EV3MotorRegulatorKM reg;
    protected TachoMotorPort tachoPort;
    protected boolean stalled = false;
    protected RegulatedMotorListener listener = null;
    protected float speed = 360;
    protected int acceleration = 6000;
    protected int limitAngle = 0;

    /**
     * Use this constructor to assign a variable of type motor connected to a particular port.
     * @param port  to which this motor is connected
     */
    public LargeRegulatedMotor(TachoMotorPort port)
    {
        //tachoPort = port;
        //tachoPort.setPWMMode(TachoMotorPort.PWM_BRAKE);
        reg = new EV3MotorRegulatorKM(port);
        //reg = new EV3MotorRegulator(port);
        //reg = new EV3MotorRegulatorFP(port);
        reg.resetTachoCount();
        //TODO: Should we take control of the motor at this point?
    }
    
    /**
     * Use this constructor to assign a variable of type motor connected to a particular port.
     * @param port  to which this motor is connected
     */
    public LargeRegulatedMotor(Port port)
    {
        this(port.open(TachoMotorPort.class));
        releaseOnClose(tachoPort);
    }

    /**
     * Close the motor regulator. Release the motor from regulation and free any
     * associated resources.
     */
    public void close()
    {
        suspendRegulation();
        super.close();
    }

    /**
     * Removes this motor from the motor regulation system. After this call
     * the motor will be in float mode and will have stopped. Note calling any
     * of the high level move operations (forward, rotate etc.), will
     * automatically enable regulation.
     * @return true iff regulation has been suspended.
     */
    public boolean suspendRegulation()
    {
        // Putting the motor into float mode disables regulation. note
        // that we wait for the operation to complete.
        reg.newMove(0, acceleration, NO_LIMIT, false, true);
        // Now wait for the motor to become inactive
        //while (reg.active)
            //Delay.msDelay(1);
        return true;
    }

    /**
     * Update the internal state of the motor.
     * @param velocity
     * @param hold
     * @param stalled
     */
    void updateState(int velocity, boolean hold, boolean stalled)
    {
        synchronized (reg)
        {
            this.stalled = stalled;
            if (listener != null)
            {
                if (velocity == 0)
                    listener.rotationStopped(this, getTachoCount(), stalled, System.currentTimeMillis());
                else
                    listener.rotationStarted(this, getTachoCount(), false, System.currentTimeMillis());
            }
        }
    }

    /**
     * @return the current tachometer count.
     * @see lejos.robotics.RegulatedMotor#getTachoCount()
     */
    public int getTachoCount()
    {
        return reg.getTachoCount();
    }

    /**
     * Returns the current position that the motor regulator is trying to
     * maintain. Normally this will be the actual position of the motor and will
     * be the same as the value returned by getTachoCount(). However in some
     * circumstances (activeMotors that are in the process of stalling, or activeMotors
     * that have been forced out of position), the two values may differ. Note that
     * if regulation has been suspended calling this method will restart it.
     * @return the current position calculated by the regulator.
     */
    public int getPosition()
    {
        return Math.round(reg.getPosition());
    }

    /**
     * @see lejos.hardware.motor.BasicMotor#forward()
     */
    public void forward()
    {
        reg.newMove(speed, acceleration, +NO_LIMIT, true, false);
    }

    /**
     * @see lejos.hardware.motor.BasicMotor#backward()
     */
    public void backward()
    {
        reg.newMove(speed, acceleration, -NO_LIMIT, true, false);
    }

    /**
     * Set the motor into float mode. This will stop the motor without braking
     * and the position of the motor will not be maintained.
     */
    public void flt()
    {
        reg.newMove(0, acceleration, NO_LIMIT, false, true);
    }
    
    /**
     * Set the motor into float mode. This will stop the motor without braking
     * and the position of the motor will not be maintained.
     * @param immediateReturn If true do not wait for the motor to actually stop
     */
    public void flt(boolean immediateReturn)
    {
        reg.newMove(0, acceleration, NO_LIMIT, false, !immediateReturn);
    }

    /**
     * Causes motor to stop, pretty much
     * instantaneously. In other words, the
     * motor doesn't just stop; it will resist
     * any further motion.
     * Cancels any rotate() orders in progress
     */
    public void stop()
    {
        reg.newMove(0, acceleration, NO_LIMIT, true, true);
    }

    /**
     * Causes motor to stop, pretty much
     * instantaneously. In other words, the
     * motor doesn't just stop; it will resist
     * any further motion.
     * Cancels any rotate() orders in progress
     * @param immediateReturn if true do not wait for the motor to actually stop
     */
    public void stop(boolean immediateReturn)
    {
        reg.newMove(0, acceleration, NO_LIMIT, true, !immediateReturn);
    }

    /**
     * This method returns <b>true </b> if the motor is attempting to rotate.
     * The return value may not correspond to the actual motor movement.<br>
     * For example,  If the motor is stalled, isMoving()  will return <b> true. </b><br>
     * After flt() is called, this method will return  <b>false</b> even though the motor
     * axle may continue to rotate by inertia.
     * If the motor is stalled, isMoving()  will return <b> true. </b> . A stall can
     * be detected  by calling {@link #isStalled()};
     * @return true iff the motor is attempting to rotate.<br>
     */
    public boolean isMoving()
    {
        return reg.isMoving();
    }

    /**
     * Wait until the current movement operation is complete (this can include
     * the motor stalling).
     */
    public void waitComplete()
    {
        synchronized(reg)
        {
            while(reg.isMoving())
                try {reg.wait();} catch(InterruptedException e){}
        }
    }

    public void rotateTo(int limitAngle, boolean immediateReturn)
    {
        reg.newMove(speed, acceleration, limitAngle, true, !immediateReturn);
    }

    /**
     * Sets desired motor speed , in degrees per second;
     * The maximum reliably sustainable velocity is  100 x battery voltage under
     * moderate load, such as a direct drive robot on the level.
     * @param speed value in degrees/sec
     */
    public void setSpeed(int speed)
    {
        this.speed = Math.abs(speed);
        reg.adjustSpeed(this.speed);
    }

    /**
     * Sets desired motor speed , in degrees per second;
     * The maximum reliably sustainable velocity is  100 x battery voltage under
     * moderate load, such as a direct drive robot on the level.
     * @param speed value in degrees/sec
     */
    public void setSpeed(float speed)
    {
        this.speed = Math.abs(speed);
        reg.adjustSpeed(this.speed);
    }

    /**
     * sets the acceleration rate of this motor in degrees/sec/sec <br>
     * The default value is 6000; Smaller values will make speeding up. or stopping
     * at the end of a rotate() task, smoother;
     * @param acceleration
     */
    public void setAcceleration(int acceleration)
    {
        this.acceleration = Math.abs(acceleration);
        reg.adjustAcceleration(this.acceleration);
    }

    /**
     * returns acceleration in degrees/second/second
     * @return the value of acceleration
     */
    public int getAcceleration()
    {
        return acceleration;
    }

    /**
     * Return the angle that this Motor is rotating to.
     * @return angle in degrees
     */
    public int getLimitAngle()
    {
        return limitAngle; // set in reg
    }

    /**
     * Reset the tachometer associated with this motor. Note calling this method
     * will cause any current move operation to be halted.
     */
    public void resetTachoCount()
    {
        synchronized(reg)
        {
            // Make sure we are stopped!
            reg.newMove(0, acceleration, NO_LIMIT, false, true);
            reg.resetTachoCount();
            reg.reset();
        }
    }

    /**
     * Add a motor listener. Move operations will be reported to this object.
     * @param listener
     */
    public void addListener(RegulatedMotorListener listener)
    {
        this.listener = listener;
    }
    
    public RegulatedMotorListener removeListener() {
        RegulatedMotorListener old = this.listener;
        this.listener = null;
        return old;
    }

    /**
     * Rotate by the request number of degrees.
     * @param angle number of degrees to rotate relative to the current position
     * @param immediateReturn if true do not wait for the move to complete
     */
    public void rotate(int angle, boolean immediateReturn)
    {
        synchronized(reg)
        {
            rotateTo(Math.round(reg.getPosition()) + angle, immediateReturn);
        }
    }

    /**
     * Rotate by the requested number of degrees. Wait for the move to complete.
     * @param angle
     */
    public void rotate(int angle)
    {
        rotate(angle, false);
    }

    /**
     * Rotate to the target angle. Do not return until the move is complete.
     * @param limitAngle Angle to rotate to.
     */
    public void rotateTo(int limitAngle)
    {
        rotateTo(limitAngle, false);
    }

    /**
     * Return the current target speed.
     * @return the current target speed.
     */
    public int getSpeed()
    {
        return Math.round(speed);
    }


    /**
     * @deprecated The regulator will always try to hold position unless the
     * motor is set into float mode using flt().
     * @param power - a value between 1 and 100;
     */
    @Deprecated
    public void lock(int power)
    {
        stop(false);
    }

    /**
     * Return true if the motor is currently stalled.
     * @return true if the motor is stalled, else false
     */
    public boolean isStalled()
    {
        return stalled;
    }

    /**
     * Set the parameters for detecting a stalled motor. A motor will be recognised
     * as stalled if the movement error (the amount the motor lags the regulated
     * position) is greater than error for a period longer than time.
     * @param error The error threshold
     * @param time The time that the error threshold needs to be exceeded for.
     */
    public void setStallThreshold(int error, int time)
    {
        reg.setStallThreshold(error, time);
    }
    /**
     * Return the current velocity.
     * @return current velocity in degrees/s
     */
    public int getRotationSpeed()
    {
        return Math.round(reg.getCurrentVelocity());
    }


    public float getMaxSpeed() {
        // It is generally assumed, that the maximum accurate speed of Motor is
        // 100 degree/second * Voltage
        return LocalEV3.ev3.battery.getVoltage() * 100.0f;
    }
}
