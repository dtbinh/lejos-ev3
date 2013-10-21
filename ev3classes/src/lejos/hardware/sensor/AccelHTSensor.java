package lejos.hardware.sensor;

import lejos.hardware.port.I2CPort;
import lejos.hardware.port.Port;
import lejos.robotics.Accelerometer;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Class to access the HiTechnic NXT Acceleration / Tilt Sensor (NAC1040).
 * 
 * Some sensors seem to be badly calibrated, so 0 is not always level.
 */
public class AccelHTSensor extends I2CSensor implements Accelerometer {
	/*
	 * Documentation: http://www.hitechnic.com/cgi-bin/commerce.cgi?preadd=action&key=NAC1040
	 * Some details from HTAC-driver.h from http://rdpartyrobotcdr.sourceforge.net/
	 * 
	 * ProductId: "HITECHNC"
	 * SensorType: "Accel."
	 * (confirmed for version " V1.1")
	 * 
	 * Tested by Michael Mirwaldt:
	 * one 4byte read is 20% faster than 2 reads of 1 byte.
	 */

	private byte[] buf = new byte[6];
	
	//TODO we might add setPrecision(boolean) method which can be used to enable/disabled the reading of the lower 2 bits
	
	private static final int BASE_ACCEL = 0x42;
	
	private static final int OFF_X_HIGH = 0x00;
	private static final int OFF_Y_HIGH = 0x01;
	private static final int OFF_Z_HIGH = 0x02;
	private static final int OFF_2BITS = 3;

	public static final int ERROR = Integer.MIN_VALUE;

	public AccelHTSensor(I2CPort port) {
		this(port, DEFAULT_I2C_ADDRESS);
	}

	public AccelHTSensor(I2CPort port, int address) {
		// TODO: Needs to be able to accept high-speed! Might be problem I was
		// having.
		super(port, address);
	}
	
    public AccelHTSensor(Port port, int address) {
        super(port, address, TYPE_LOWSPEED_9V);
    }

    public AccelHTSensor(Port port) {
        this(port, DEFAULT_I2C_ADDRESS);
    }

	/**
	 * Acceleration along X axis. Positive or negative values.
	 * A value of 200 is equivalent to 1g.
	 * 
	 * @return x-axis acceleration or {@link #ERROR}.
	 */
	public int getXAccel() {
		getData(BASE_ACCEL + OFF_X_HIGH, buf, 0, OFF_2BITS + 1);

		return (buf[0] << 2) | (buf[OFF_2BITS] & 0xFF);
	}

	/**
	 * Acceleration along Y axis. Positive or negative values.
	 * A value of 200 is equivalent to 1g.
	 * 
	 * @return y-axis acceleration or {@link #ERROR}.
	 */
	public int getYAccel() {
		getData(BASE_ACCEL + OFF_Y_HIGH, buf, 0, OFF_2BITS + 1);

		return (buf[0] << 2) | (buf[OFF_2BITS] & 0xFF);
	}

	/**
	 * Acceleration along Z axis. Positive or negative values.
	 * A value of 200 is equivalent to 1g.
	 * 
	 * @return z-axis acceleration or {@link #ERROR}.
	 */
	public int getZAccel() {
		getData(BASE_ACCEL + OFF_Z_HIGH, buf, 0, OFF_2BITS + 1);

		return (buf[0] << 2) | (buf[OFF_2BITS] & 0xFF);
	}

	/**
	 * Reads all 3 acceleration values into the given array.
	 * Elements off+0, off+1, and off+2 are filled with X, Y, and Z axis.
	 * @param dst destination array.
	 * @param off offset
	 * @return true on success, false on error
	 */
	public boolean getAllAccel(int[] dst, int off) {
		getData(BASE_ACCEL, buf, 0, 6);
		
		dst[off+0] = (buf[OFF_X_HIGH] << 2) + (buf[OFF_X_HIGH + OFF_2BITS] & 0xFF);
		dst[off+1] = (buf[OFF_Y_HIGH] << 2) + (buf[OFF_Y_HIGH + OFF_2BITS] & 0xFF);
		dst[off+2] = (buf[OFF_Z_HIGH] << 2) + (buf[OFF_Z_HIGH + OFF_2BITS] & 0xFF);
		return true;
	}
}