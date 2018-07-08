package edu.ntu.networking.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Forwarder extends Thread {

  private String siteid = null;
  private DataInputStream smdis = null;
  private DataOutputStream smdos = null;
  private int quality = 0;
  private int err_count = 0;

  public Forwarder(DataInputStream var1, DataOutputStream var2) {
    this.smdis = var1;
    this.smdos = var2;
  }

  public void setQuality(int var1) {
    this.quality = var1;
  }

  public void run() {
    PFrameMsg var1 = new PFrameMsg();

    while (true) {
      this.get_frame(var1);
      this.forward_frame(var1);
    }
  }

  private void get_frame(PFrameMsg var1) {
    try {
      var1.receive(this.smdis);
      if (var1.kind == 4) {
        this.siteid = var1.info.data;
      }
    } catch (Exception var3) {
      System.out.println("Error on receiving frame: " + var3 + " NetSim quitting....");
      System.exit(0);
    }

  }

  private void forward_frame(PFrameMsg var1) {
    if (var1.kind == 4) {
      try {
        var1.send(this.smdos);
      } catch (Exception var5) {
        System.out.println("Error on forwarding connection frame: " + var5);
        System.exit(0);
      }

    } else {
      double var2 = Math.random();

      try {
        switch (this.quality) {
          case 0:
            var1.send(this.smdos);
            break;
          case 1:
            if (var2 < 0.75D) {
              var1.send(this.smdos);
            } else {
              ++this.err_count;
              System.out.println(
                  "VMach " + this.siteid + " loose frame seq = " + var1.seq + " error counter = "
                      + this.err_count);
              System.out.flush();
            }
            break;
          case 2:
            if (var2 < 0.75D) {
              var1.send(this.smdos);
            } else {
              ++this.err_count;
              System.out.println("VMach " + this.siteid + " Check sum error for seq = " + var1.seq
                  + " error counter = " + this.err_count);
              System.out.flush();
              var1.kind = 3;
              var1.send(this.smdos);
            }
            break;
          case 3:
            if (var2 < 0.5D) {
              var1.send(this.smdos);
            } else if (var2 < 0.75D) {
              ++this.err_count;
              System.out.println("VMach " + this.siteid + " Check sum error for seq = " + var1.seq
                  + " error counter = " + this.err_count);
              System.out.flush();
              var1.kind = 3;
              var1.send(this.smdos);
            } else {
              ++this.err_count;
              System.out.println(
                  "VMach " + this.siteid + " loose frame seq = " + var1.seq + " error counter = "
                      + this.err_count);
              System.out.flush();
            }
            break;
          default:
            System.out.println("VMach " + this.siteid + " undefined quality level " + this.quality);
        }
      } catch (Exception var6) {
        System.out.println("Error on forwarding frame: " + var6);
      }

    }
  }
}
