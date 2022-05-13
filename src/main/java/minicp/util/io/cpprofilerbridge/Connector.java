package minicp.util.io.cpprofilerbridge;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;

public class Connector {
  // ATTRIBUTE
  private Socket clientSocket;
  private DataOutputStream out;
  private Message msg;
  private boolean DEBUG;

  //CONSTRUCTOR
  public Connector() {
    System.out.println("\n---------------------------------------");
    System.out.println("MiniCP-CPProfiler Connector initialized");
    this.DEBUG = true;
  }

  // METHODS
  public enum NodeStatus {
    SOLVED(0),
    FAILED(1),
    BRANCH(2),
    SKIPPED(6);
    private final int id;
    private NodeStatus(int id) { this.id = id; }
    public int getNumber() { return id; }
  }

  public void connect(int port) throws IOException {
    this.clientSocket = new Socket("localhost", port);
    this.out = new DataOutputStream(clientSocket.getOutputStream());
    this.msg = new Message(this);
    System.out.println("Connected to 'localhost:'" + port + "\n");
  }

  public void disconnect() throws IOException, InterruptedException {
    msg.setType(Message.MsgType.DONE);
    sendThroughSocket(msg.toBytes());
    //testMsg(msg.toBytes());
    msg.clear();
    // 01 -> CLOSE STREAM
    out.close();
    // 02 -> CLOSE SOCKET
    clientSocket.close();
    System.out.println("\nMiniCP-CPProfiler Connector closed");
    System.out.println("---------------------------------------\n");
  }

  public void start(int rid) throws IOException, InterruptedException {
    start("", rid);
  }

  public void start(String file_name, int rid) throws IOException, InterruptedException {
    msg = msg.setType(Message.MsgType.START).setLabel(file_name).setRestartId(rid);
    sendThroughSocket(msg.toBytes());
    msg.clear();
  }

  public void restart(int rid) throws IOException, InterruptedException {
    restart("", rid);
  }

  public void restart(String file_name, int rid) throws IOException, InterruptedException {
    msg = msg.setType(Message.MsgType.RESTART).setLabel(file_name).setRestartId(rid);
    sendThroughSocket(msg.toBytes());
    msg.clear();
  }

  private Message createNewNode(int sid, int pid, int alt, int kids, NodeStatus status) {
    return msg.setType(Message.MsgType.NODE)
    .setNodeId(sid)
    .setNodePid(pid)
    .setNodeAlt(alt)
    .setNodeChildren(kids)
    .setNoteStatus(status.getNumber());
  }

  public Message createNode(int sid, int pid, int alt, int kids, NodeStatus status) {
    return this.createNewNode(sid, pid, alt, kids, status);
  }

  public void sendNode(int sid, int pid, int alt, int kids, NodeStatus status) throws IOException, InterruptedException {
    Message msg = createNewNode(sid, pid, alt, kids, status);
    sendThroughSocket(msg.toBytes());
    msg.clear();
  }

  public void sendNode(Message msg) throws IOException, InterruptedException {
    sendThroughSocket(msg.toBytes());
    msg.clear();
  }

  private synchronized void sendThroughSocket(byte[] msg) throws IOException, InterruptedException {
    int msg_size = msg.length;
    byte[] size_buffer = new byte[4];
    ByteBuffer.wrap(size_buffer).order(ByteOrder.LITTLE_ENDIAN).putInt(msg_size);

    if(DEBUG) {
      System.out.print("SENT: ");
      System.out.print(bytesToString(size_buffer));
      System.out.println(bytesToString(msg));
    }

    // 01 -> SEND MSG SIZE
    out.write(size_buffer);
    // 02 -> SEND MSG NOW
    out.write(msg);
    //out.flush();
    //TimeUnit.SECONDS.sleep(1);
  }

  private void testMsg(byte[] msg) throws IOException {

    byte[] b = msg;
    // int size = b.length;
    // // //byte[] size_buffer = new byte[4];
    // byte[] size_buffer = new byte[4];

    // for (int i = 0; i < 4; i++) {
    //      size_buffer[i] = (byte)(size >>> (i * 8));
    //      System.out.println(size_buffer[i]);
    // }
    //byte[] b = {};
    // // clientSocket.getOutputStream().write(size_buffer);

    // // clientSocket.getOutputStream().write(b);

    byte[] b_size = ByteBuffer.allocate(4).putInt(b.length).array();





    //System.out.format(msg.toString());
    DataOutputStream out    = new DataOutputStream(clientSocket.getOutputStream());
    // byte[] bytes = ByteBuffer.allocate(4).putInt(1795830244).array();

    byte[] size_bitwz = new byte[4];
    for (int i = 0; i < 4; i++) {
      size_bitwz[i] = (byte)(33 >>> (i * 8));
      System.out.println(size_bitwz[i]);
    }

    // for (byte o : to_send) {
    //   System.out.format("0x%x ", o);
    // }
    //byte[] test_ = { 0x21, 0x0, 0x0, 0x0, 0x02, 0x02, 0x00, 0x00, 00, 0x1B, 0x7b, 0x22, 0x6e, 0x61, 0x6d, 0x65, 0x22, 0x3a, 0x20, 0x22, 0x6d, 0x69 ,0x6e ,0x69 ,0x6d, 0x61, 0x6c ,0x20, 0x65 ,0x78, 0x61 ,0x6d ,0x70 ,0x6c ,0x65 ,0x22 ,0x7d};
    byte[] test_ = { 0x02, 0x02, 0x00, 0x00, 00, 0x1B, 0x7b, 0x22, 0x6e, 0x61, 0x6d, 0x65, 0x22, 0x3a, 0x20, 0x22, 0x6d, 0x69 ,0x6e ,0x69 ,0x6d, 0x61, 0x6c ,0x20, 0x65 ,0x78, 0x61 ,0x6d ,0x70 ,0x6c ,0x65 ,0x22 ,0x7d};
    System.out.print("test_.length");
    System.out.print(test_.length);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
    outputStream.write( size_bitwz );
    outputStream.write( test_ );
    byte[] to_send = outputStream.toByteArray();

    byte[] test_2 = {  0x2B, 0x0 , 0x0 , 0x0 , 0x0 , 0x0 , 0x0 , 0x0 , 0x0 , 0x0, 0x0, 0x0, 0x0 ,0x0,  (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, (byte)0x0, 0x00 , 0x00 , 0x00 , 0x02, 0x02 , 0x0 , 0x0 , 0x0 , 0x0 , 0x04 , 0x52 , 0x6f , 0x6f , 0x74};

    byte[] test_3 = { 0x01 ,0x0 , 0x0 , 0x0 ,  0x01};
    // for (int i = 0; i < b.length; i++) {
    //   System.out.print(b[i]);
    //   System.out.print("-");
    // }
    // ByteBuffer _b = ByteBuffer.allocate(4);
    // //b.order(ByteOrder.BIG_ENDIAN); // optional, the initial order of a byte buffer is always BIG_ENDIAN.
    // _b.putInt(b.length);

    // byte[] result = _b.array();
    //out.write(10);
    out.write(to_send);
    out.write(test_2);
    out.write(test_3);
    //clientSocket.close();
    //out.close();
  }

  // ---------- FOR DEBUG -----------
  private int unsignedByteToInt(byte b) {
    return (int) b & 0xFF;
  }

  public String bytesToString(byte[] bytes) {
    StringBuilder sb = new StringBuilder(4 * bytes.length);
    sb.append("[");

    for (int i = 0; i < bytes.length; i++) {
      sb.append(this.unsignedByteToInt(bytes[i]));
      if (i + 1 < bytes.length) {
        sb.append(",");
      }
    }

    sb.append("]");
    return sb.toString();
  }
  //--------------------------------------
}