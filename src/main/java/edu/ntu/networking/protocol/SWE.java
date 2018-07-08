package edu.ntu.networking.protocol;

import edu.ntu.networking.protocol.event.EventQueue;
import edu.ntu.networking.protocol.event.PEvent;
import edu.ntu.networking.protocol.packet.Packet;
import edu.ntu.networking.protocol.packet.PacketQueue;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class SWE {

  private Socket myso = null;
  private DataInputStream mydis = null;
  private DataOutputStream mydos = null;
  private EventQueue equeue = new EventQueue();
  private PacketQueue pqueue = new PacketQueue();
  private int credit = 0;
  private String siteid = null;

  public SWE(String var1) {
    this.siteid = var1;
  }

  public void init() {
    try {
      System.out.println("VMach is making a connection with NetSim...");
      this.myso = new Socket(InetAddress.getLocalHost(), 54321);
      System.out.println(
          "VMach(" + this.myso.getLocalPort() + ") <===> NetSim(" + this.myso.getInetAddress() + ":"
              + this.myso.getPort() + ")");
      this.mydos = new DataOutputStream(this.myso.getOutputStream());
      this.mydis = new DataInputStream(this.myso.getInputStream());
      PFrameMsg var1 = new PFrameMsg();
      var1.kind = 4;
      var1.info.data = this.siteid;
      var1.send(this.mydos);
      var1.receive(this.mydis);
      FrameHandler var2 = new FrameHandler(this.mydis, this);
      var2.start();
      NetworkSender var3 = new NetworkSender(this, this.siteid);
      var3.start();
      NetworkReceiver var4 = new NetworkReceiver(this, this.siteid);
      var4.start();
    } catch (IOException var5) {
      System.err.println("ERROR: unable to make connection: " + var5 + " quitting...");
      System.exit(0);
    } catch (Exception var6) {
      System.err.println("ERROR in SWE(): " + var6 + " quitting ....");
      System.exit(0);
    }

  }

  public void wait_for_event(PEvent var1) {
    PEvent var2 = this.equeue.get();
    var1.type = var2.type;
    var1.seq = var2.seq;
  }

  public void to_datalink_layer(Packet var1) {
    PEvent var2 = new PEvent();
    PFrame var3 = new PFrame();
    var2.type = 1;
    var3.info = var1;
    var2.frame = var3;
    this.equeue.put(var2);
  }

  public void from_network_layer(Packet var1) {
    PFrame var2 = this.equeue.get_frame();
    var1.data = var2.info.data;
  }

  public void to_physical_layer(PFrame var1) {
    PFrameMsg var2 = new PFrameMsg();
    var2.kind = var1.kind;
    var2.ack = var1.ack;
    var2.info = var1.info;
    var2.seq = var1.seq;

    try {
      var2.send(this.mydos);
    } catch (Exception var4) {
      System.out.println("SWE: Error on sending frame: " + var4);
    }

  }

  public PFrame from_physical_layer() {
    PFrame var1 = this.equeue.get_frame();
    return var1;
  }

  public void to_network_layer(Packet var1) {
    this.pqueue.put(var1);
  }

  public void from_datalink_layer(Packet var1) {
    Packet var2 = this.pqueue.get();
    var1.data = var2.data;
  }

  public synchronized void get_credit() {
    while (this.credit <= 0) {
      try {
        this.wait();
      } catch (InterruptedException var2) {
        ;
      }
    }

    --this.credit;
  }

  public synchronized void grant_credit(int var1) {
    this.credit += var1;
    this.notify();
  }

  public void generate_timeout_event(int var1) {
    PEvent var2 = new PEvent();
    var2.type = 2;
    var2.seq = var1;
    this.equeue.put(var2);
  }

  public void removeTimer(int var1) {
    this.equeue.removeTimer(var1);
  }

  public void generate_acktimeout_event() {
    PEvent var1 = new PEvent();
    var1.type = 3;
    this.equeue.put(var1);
  }

  public void removeAckTimer() {
    this.equeue.removeAckTimer();
  }

  public void generate_frame_arrival_event(PFrame var1) {
    PEvent var2 = new PEvent();
    if (var1.kind == 3) {
      var2.type = 4;
    } else {
      var2.type = 0;
    }

    var2.frame = var1;
    this.equeue.put(var2);
  }
}