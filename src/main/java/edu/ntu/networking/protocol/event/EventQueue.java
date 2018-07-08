package edu.ntu.networking.protocol.event;

import edu.ntu.networking.protocol.PFrame;

import java.util.Vector;

public class EventQueue {

  private Vector queue = new Vector(8);
  private PFrame fm = null;

  public EventQueue() {
  }

  public synchronized void put(PEvent var1) {
    this.queue.addElement(var1);
    this.notify();
  }

  public synchronized PEvent get() {
    while (this.queue.isEmpty()) {
      try {
        this.wait();
      } catch (InterruptedException var2) {
        ;
      }
    }

    PEvent var1 = (PEvent) this.queue.firstElement();
    this.queue.removeElementAt(0);
    this.fm = var1.frame;
    return var1;
  }

  public PFrame get_frame() {
    if (this.fm == null) {
      System.out.println("EventQue: wait_for_event()must be called before from_physical_layer()");
    }

    System.out.flush();
    return this.fm;
  }

  public synchronized void removeTimer(int var1) {
    int var2 = this.queue.size();

    for (int var3 = 0; var3 < var2; ++var3) {
      PEvent var4 = (PEvent) this.queue.elementAt(var3);
      System.out.println("EventQue: event: type = " + PEvent.KIND[var4.type] + "seq = " + var4.seq);
      System.out.flush();
      if (var4.type == 2 && var4.seq == var1) {
        this.queue.removeElementAt(var3);
        --var2;
        System.out.println("EventQue: removed time out event for seq: " + var1);
        System.out.flush();
        break;
      }
    }

  }

  public synchronized void removeAckTimer() {
    int var1 = this.queue.size();

    for (int var2 = 0; var2 < var1; ++var2) {
      PEvent var3 = (PEvent) this.queue.elementAt(var2);
      if (var3.type == 3) {
        this.queue.removeElementAt(var2);
        --var1;
        System.out.println("EventQue: removed ack time out event");
        System.out.flush();
      }
    }

  }
}
