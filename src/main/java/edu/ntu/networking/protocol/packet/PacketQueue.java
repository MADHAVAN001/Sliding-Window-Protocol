package edu.ntu.networking.protocol.packet;

import java.util.Vector;

public class PacketQueue {

  private Vector queue = new Vector(8);

  public PacketQueue() {
  }

  public synchronized void put(Packet var1) {
    this.queue.addElement(var1);
    this.notify();
  }

  public synchronized Packet get() {
    while (this.queue.isEmpty()) {
      try {
        this.wait();
      } catch (InterruptedException var2) {
        ;
      }
    }

    Packet var1 = (Packet) this.queue.firstElement();
    this.queue.removeElementAt(0);
    return var1;
  }
}