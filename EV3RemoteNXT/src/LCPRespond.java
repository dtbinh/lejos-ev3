import lejos.hardware.Bluetooth;
import lejos.remote.nxt.LCPResponder;
import lejos.remote.nxt.NXTCommConnector;

public class LCPRespond {
  /**
   * Our local Responder class so that we can over-ride the standard behaviour.
   * We modify the disconnect action so that the thread will exit.
   */
  static class Responder extends LCPResponder {
    Responder(NXTCommConnector con) {
      super(con);
    }

    protected void disconnect() {
      super.disconnect();
      super.shutdown();
    }
  }

  public static void main(String[] args) throws Exception {
    try {
      Responder resp = new Responder(Bluetooth.getNXTCommConnector());
      resp.start();
      resp.join();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
