package edu.ntu.networking.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PFrameMsg extends PFrame {

  public static final int CKSUM_ERR = 3;
  public static final int CONNECT = 4;

  public PFrameMsg() {
  }

  public void send(DataOutputStream var1) throws IOException {
    var1.writeInt(super.kind);
    var1.writeInt(super.seq);
    var1.writeInt(super.ack);
    var1.writeUTF(super.info.data);
  }

  public void receive(DataInputStream var1) throws IOException {
    super.kind = var1.readInt();
    super.seq = var1.readInt();
    super.ack = var1.readInt();
    super.info.data = var1.readUTF();
  }
}
