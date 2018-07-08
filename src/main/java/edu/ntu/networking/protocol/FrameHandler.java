package edu.ntu.networking.protocol;

import java.io.DataInputStream;

public class FrameHandler extends Thread {

  private DataInputStream smdis = null;
  private SWE swe = null;

  public FrameHandler(DataInputStream var1, SWE var2) {
    this.smdis = var1;
    this.swe = var2;
  }

  public void run() {
    PFrameMsg var1 = new PFrameMsg();

    while (true) {
      this.get_frame(var1);
      this.swe.generate_frame_arrival_event(var1);
      var1 = new PFrameMsg();
    }
  }

  private void get_frame(PFrameMsg var1) {
    try {
      var1.receive(this.smdis);
    } catch (Exception var3) {
      System.out.println("FrameHandler: Error on receiving frame: " + var3 + " quitting...");
      System.exit(0);
    }

  }
}