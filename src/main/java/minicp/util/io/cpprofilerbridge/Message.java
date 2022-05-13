package minicp.util.io.cpprofilerbridge;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public final class Message {
    // ATTRIBUTE
    private int msgType;
    private int restartId;
    private int nodeId;
    private int nodePid;
    private int nodeAlt;
    private int nodeChildren;
    private int nodeStatus;
    private String nodeLabel;
    private String nodeNoGood;
    private String nodeInfo;
    private String restartLabel;
    private Connector connector_;

    // ENUMERATION
    public enum MsgType {
        NODE(0),
        DONE(1),
        START(2),
        RESTART(3);

        private final int id;
        private MsgType(int id) { this.id = id; }
        public int getNumber() { return id; }
    }

    public enum OptionalArgs {
        LABEL(0),
        NOGOOD(1),
        INFO(2);

        private final int id;
        private OptionalArgs(int id) { this.id = id; }
        public int getNumber() { return id; }
    }

    // CONSTRUCTOR
    public Message(Connector connector) {
        this.connector_ = connector;
        this.clear();
    }

    // SETTER
    public Message setType(MsgType type) {
        this.msgType = type.getNumber();
        return this;
    }
    public Message setLabel(String label) {
        this.restartLabel = label;
        return this;
    }
    public Message setRestartId(int id) {
        this.restartId = id;
        return this;
    }
    public Message setNodePid(int pid) {
        this.nodePid = pid;
        return this;
    }
    public Message setNodeAlt(int alt) {
        this.nodeAlt = alt;
        return this;
    }
    public Message setNodeChildren(int c) {
        this.nodeChildren = c;
        return this;
    }
    public Message setNoteStatus(int status) {
        this.nodeStatus = status;
        return this;
    }
    public Message setNodeId(int id) {
        this.nodeId = id;
        return this;
    }
    public Message setNodeRestartId(int id) {
        this.nodeId = id;
        return this;
    }
    public Message setNodeLabel(String data) {
        this.nodeLabel = data;
        return this;
    }
    public Message setNodeNoGood(String data) {
        this.nodeNoGood = data;
        return this;
    }
    public Message setNodeInfo(String data) {
        this.nodeInfo = data;
        return  this;
    }
    public void send() {
        try {
            this.connector_.sendNode(this);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // PUBLIC METHODS
    public byte[] toBytes() throws IOException {
        System.out.println("\n"+this.toString()+"\n");
        if(this.msgType == MsgType.DONE.getNumber()) {
            return this.convertToBytes(MsgType.DONE.getNumber(), 1);
        }
        else if(this.msgType == MsgType.START.getNumber()){
            // CREATE OUTPUT STREAM
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );

            // LET PREPARE REQUIRED ARGS
            byte[] msg_type = this.convertToBytes(MsgType.START.getNumber(), 1);
            outputStream.write( msg_type );

            // OPTIONAL PARAMETERS
            if(restartLabel != "-") {
                byte[] msg_info = this.convertToBytes(OptionalArgs.INFO.getNumber(), 1);

                // CONFIG FZN FILE TITLE(CPP SIDE)
                String reformat_data = "{\"name\": \"" + this.restartLabel + "\"}";
                byte[] msg_data = reformat_data.getBytes(StandardCharsets.UTF_8);

                // INFO DATA SIZE
                byte[] size_data = this.convertToBytes(msg_data.length, 4, "BIG_ENDIAN");

                // GROUPED DATA
                outputStream.write( msg_info );
                outputStream.write( size_data );
                outputStream.write( msg_data );
            }
            return outputStream.toByteArray();
        }
        else if(this.msgType == MsgType.RESTART.getNumber()){
            byte[] msg_type = this.convertToBytes(MsgType.RESTART.getNumber(), 1);
            return msg_type;
        }
        else if(this.msgType == MsgType.NODE.getNumber()){
            // CREATE OUTPUT STREAM
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );

            // LET PREPARE REQUIRED ARGS
            byte[] msg_type = this.convertToBytes(MsgType.NODE.getNumber(), 1);
            // Id-[12bytes]
            byte[] node_id = this.convertToBytes(this.nodeId, 4, "BIG_ENDIAN");
            byte[] node_restart_id = this.convertToBytes(restartId, 4,"BIG_ENDIAN");
            byte[] node_thread_id = this.convertToBytes(-1, 4,"BIG_ENDIAN");
            // Pid-[12bytes]
            byte[] parent_id = this.convertToBytes(this.nodePid, 4,"BIG_ENDIAN");
            byte[] parent_restart_id = this.convertToBytes(restartId, 4,"BIG_ENDIAN");
            byte[] parent_thread_id = this.convertToBytes(-1, 4,"BIG_ENDIAN");
            // Alt-&-kids-[4bytes]
            byte[] msg_alt = this.convertToBytes(this.nodeAlt, 4,"BIG_ENDIAN");
            byte[] msg_kid = this.convertToBytes(this.nodeChildren, 4,"BIG_ENDIAN");
            // Status-[1bytes]
            byte[] msg_status = this.convertToBytes(nodeStatus, 1);

            // WRITE REQUIRED ARGS
            outputStream.write( msg_type );
            outputStream.write( node_id );
            outputStream.write( node_restart_id );
            outputStream.write( node_thread_id );
            outputStream.write( parent_id );
            outputStream.write( parent_restart_id );
            outputStream.write( parent_thread_id );
            outputStream.write( msg_alt );
            outputStream.write( msg_kid );
            outputStream.write( msg_status );

            // LET PREPARE OPTIONAL ARGS
            if(nodeLabel != "-") {
                byte[] msg_label = this.convertToBytes(OptionalArgs.LABEL.getNumber(), 1);
                byte[] msg_data1 = nodeLabel.getBytes(StandardCharsets.UTF_8);
                byte[] size_data1 = this.convertToBytes(msg_data1.length, 4, "BIG_ENDIAN");
                outputStream.write( msg_label );
                outputStream.write( size_data1 );
                outputStream.write( msg_data1 );
            }
            if(nodeNoGood != "-") {
                byte[] msg_nogood = this.convertToBytes(OptionalArgs.NOGOOD.getNumber(), 1);
                byte[] msg_data2 = nodeLabel.getBytes(StandardCharsets.UTF_8);
                byte[] size_data2 = this.convertToBytes(msg_data2.length, 4, "BIG_ENDIAN");
                outputStream.write( msg_nogood );
                outputStream.write( size_data2 );
                outputStream.write( msg_data2 );
            }
            if(nodeInfo != "-") {
                byte[] msg_info = this.convertToBytes(OptionalArgs.INFO.getNumber(), 1);

                // CONFIG FZN FILE TITLE(CPP SIDE)
                String reformat_data = "{\"name\": \"" + this.nodeInfo + "\"}";
                byte[] msg_data3 = reformat_data.getBytes(StandardCharsets.UTF_8);

                // INFO DATA SIZE
                byte[] size_data3 = this.convertToBytes(msg_data3.length, 4, "BIG_ENDIAN");

                outputStream.write( msg_info );
                outputStream.write( size_data3 );
                outputStream.write( msg_data3 );
            }

            //SEND FINAL RESULT
            return outputStream.toByteArray();
        }
        else{
            // DEFAULT STATE, END GRAPH BUILD
            return this.convertToBytes(MsgType.DONE.getNumber(), 1);
        }
    }

    public void clear() {
        msgType = 0;
        //restartId = -1;
        nodeId = 0;
        nodePid = 0;
        nodeAlt = 0;
        nodeChildren = 0;
        nodeStatus = 0;
        nodeLabel = "-";
        nodeNoGood = "-";
        nodeInfo = "-";
        restartLabel = "-";
    }

    public String toString() {
        return "{\n" +
                "\t'msgType': "+msgType+",\n" +
                "\t'nodeId': "+nodeId+",\n" +
                "\t'nodePid': "+nodePid+",\n" +
                "\t'nodeAlt': "+nodeAlt+",\n" +
                "\t'nodeChildren': "+nodeChildren+",\n" +
                "\t'nodeStatus': "+nodeStatus+",\n" +
                "\t'nodeLabel': "+nodeLabel+",\n" +
                "\t'nodeNoGood': "+nodeNoGood+",\n" +
                "\t'nodeInfo': "+nodeInfo+",\n" +
                "\t'restartId': "+restartId+",\n" +
                "\t'restartLabel': "+restartLabel+"\n}";
    }

    //PRIVATE METHODS
    private byte[] convertToBytes(int data, int byte_size){
        byte[] msg_bytes_tab = new byte[byte_size];
        for (int i = 0; i < byte_size; i++) {
            msg_bytes_tab[i] = (byte)((data >>> (i * 8) & 0xff));
        }
        return msg_bytes_tab;
    }
    private byte[] convertToBytes(int data, int byte_size, String BIG_ENDIAN){
        byte[] msg_bytes_tab = new byte[byte_size];
        ByteBuffer.wrap(msg_bytes_tab).order(ByteOrder.BIG_ENDIAN).putInt(data);
        return msg_bytes_tab;
    }
    private byte[] intToBytes_( final int i ) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(i);
        return bb.array();
    }
    private byte[] intToByteArray ( final int i ) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(i);
        dos.flush();
        return bos.toByteArray();
    }
    private byte[] intToBytes(final int data) {
        return new byte[] {
                (byte)((data >> 24) & 0xff),
                (byte)((data >> 16) & 0xff),
                (byte)((data >> 8) & 0xff),
                (byte)((data) & 0xff),
        };
    }
}
