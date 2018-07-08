package edu.ntu.networking.protocol;

import edu.ntu.networking.protocol.packet.Packet;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class NetworkSender extends Thread {

  private SWE swe = null;
  private String sendfile = null;
  private BufferedReader inputStream = null;

  public NetworkSender(SWE var1, String var2) {
    this.swe = var1;
    this.sendfile = "send_file_" + var2 + ".txt";
  }

  public void run() {
    this.init();

    for (Packet var1 = this.produce_packet(); var1.data != null; var1 = this.produce_packet()) {
      this.swe.get_credit();
      this.swe.to_datalink_layer(var1);
    }

  }

  private void init() {
    try {
      this.inputStream = new BufferedReader(new FileReader(this.sendfile));
    } catch (FileNotFoundException var3) {
      System.out.println("NetworkSender: File not found quitting...");
      System.exit(0);
    } catch (IOException var4) {
      System.out.println("Error from init(): " + var4 + " quitting...");
      System.exit(0);
    }

  }

  private Packet produce_packet() {
    Packet var1 = new Packet();

    try {
      var1.data = this.inputStream.readLine();
    } catch (IOException var3) {
      System.out.println("NetworkSender: Error from produce_packet(): " + var3);
    }

    return var1;
  }
}