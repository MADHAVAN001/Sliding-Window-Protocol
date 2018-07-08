package edu.ntu.networking.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class NetSim {

  public static final int PORT = 54321;
  public static final int NUM = 2;

  public NetSim() {
  }

  public static void main(String[] var0) {
    int var1 = 0;

    try {
      var1 = Integer.parseInt(var0[0]);
    } catch (Exception var11) {
      System.err.println("\nERROR: Quality level required!");
      System.err.println("\nUsage: java NetSim [quality level] where quality level = 0 -> 3");
      System.exit(0);
    }

    if (var1 < 0 || var1 > 3) {
      System.err.println("\nERROR: Invalid quality level: " + var1);
      System.err.println("\nUsage: java NetSim [quality level] where quality level = 0 -> 3");
      System.exit(0);
    }

    Socket[] var2 = new Socket[2];
    DataInputStream[] var3 = new DataInputStream[2];
    DataOutputStream[] var4 = new DataOutputStream[2];
    ServerSocket var5 = null;

    try {
      var5 = new ServerSocket(54321);
    } catch (Exception var10) {
      System.err.println("NetSim: ServerSocket()" + var10);
      System.exit(0);
    }

    for (int var6 = 0; var6 < 2; ++var6) {
      try {
        System.err.println("NetSim(Port= 54321) is waiting for connection ... ");
        var2[var6] = var5.accept();
        var3[var6] = new DataInputStream(var2[var6].getInputStream());
        var4[var6] = new DataOutputStream(var2[var6].getOutputStream());
        System.err.println(
            "NetSim accepted connection from: " + var2[var6].getInetAddress().getHostName() + " : "
                + var2[var6].getPort());
      } catch (Exception var9) {
        System.err.println("NetSim: accept Exception " + var9);
        System.exit(0);
      }
    }

    for (int var7 = 0; var7 < 2; ++var7) {
      Forwarder var8 = new Forwarder(var3[var7], var4[1 - var7]);
      var8.setQuality(var1);
      var8.start();
    }

  }
}
