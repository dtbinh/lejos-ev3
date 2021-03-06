import java.io.File;
import java.io.FileInputStream;

import lejos.hardware.Audio;
import lejos.hardware.Sound;
import lejos.hardware.Sounds;
import lejos.remote.ev3.RemoteEV3;
import lejos.utility.Delay;

/**
 * Test of remote sound
 * 
 * @author Lawrie Griffiths
 *
 */
public class RemoteSound {
	
	public static void main(String[] args) throws Exception {
		RemoteEV3 ev3 = new RemoteEV3("192.168.0.9");
		ev3.setDefault();
		
		Audio sound = ev3.getAudio();
		
		for(int i=0;i<5;i++) {
			sound.systemSound(i);
			Delay.msDelay(1000);
		}
		
		sound.playTone(500, 2000, 100);
		
		sound.playNote(Sounds.PIANO, 2000, 1000);
		
		// Play the file on the EV3
		sound.playSample(new File("/home/lejos/programs/Trumpet.wav"));
		
		// PLay the file ob the PC
		File f = new File("Trumpet.wav");
		FileInputStream in = new FileInputStream(f);
		byte[] data = new byte[(int)f.length()];
	    in.read(data);
	    in.close();
	    sound.playSample(data, 0, (int) f.length(), 8000, 100);
	}
}
