package lejos.remote.ev3;

import java.lang.reflect.Constructor;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.robotics.SampleProvider;

public class RMIRemoteSampleProvider extends UnicastRemoteObject implements RMISampleProvider {

	private static final long serialVersionUID = -8432755905878519147L;
	private SampleProvider provider;
	private String modeName;

	protected RMIRemoteSampleProvider(String portName, String sensorName, String modeName) throws RemoteException {
		super(0);
    	try {
			Class<?> c = Class.forName(sensorName);
			Class<?>[] params = new Class<?>[1];
			params[0] = Port.class;
			Constructor<?> con = c.getConstructor(params);
			Object[] args = new Object[1];
			args[0] = LocalEV3.get().getPort(portName);
			provider = (SampleProvider) con.newInstance(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public float[] fetchSample() throws RemoteException {
		int sampleSize = provider.sampleSize();
		float[] sample = new float[sampleSize];
		if (provider.fetchSample(sample, 0) != 0) sample = null;
		return sample;
	}

}
