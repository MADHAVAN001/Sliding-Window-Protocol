package edu.ntu.networking.protocol;

import edu.ntu.networking.protocol.packet.Packet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class NetworkReceiver extends Thread {

  private PrintWriter outputStream = null;
  private SWE swe = null;
  private String receivefile = null;

  public NetworkReceiver(SWE var1, String var2) {
    this.swe = var1;
    this.receivefile = "receive_file_" + var2 + ".txt";
  }

  public void run() {
    this.init();
    Packet var1 = new Packet();

    while (true) {
      this.swe.from_datalink_layer(var1);
      this.process_packet(var1);
    }
  }

  private void init() {
    try {
      File var1 = new File(this.receivefile);
      if (var1.exists()) {
        var1.delete();
      }

      var1.createNewFile();
      this.outputStream = new PrintWriter(new FileOutputStream(var1));
    } catch (FileNotFoundException var3) {
      System.out.println("NetworkReceiver: File not found quitting..");
      System.exit(0);
    } catch (IOException var4) {
      System.out.println("Error from init(): " + var4 + " quitting...");
      System.exit(0);
    }

  }

  private void process_packet(Packet var1) {
    this.outputStream.println(var1.data);
    this.outputStream.flush();
  }
}