import java.io.*;
import java.net.*;
import java.util.HashMap;

class Peer{

    private DatagramSocket socket;
    private InetAddress address;
    private byte[] buf;
    public HashMap<InetAddress, Integer> peers = new HashMap<InetAddress, Integer>();

    public Peer() throws IOException{
        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
    }

    public void registerTracker(String msg) throws IOException {
        //send packet to tracker
        buf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 1234);
        socket.send(packet);

        //receive packet and return
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        String received = new String(packet.getData(), 0, packet.getLength());
        String[] peerStrings = received.split(",");
        for(int i=0;i<peerStrings.length;i++){
            peers.put(InetAddress.getByName(peerStrings[i].split(":")[0]),Integer.parseInt(peerStrings[i].split(":")[1]));
        }

    }

    public void leaveChat() throws IOException {
        //send packet to tracker
        String msg="disconnect";
        buf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 1234);
        socket.send(packet);
    }
    

    public static void main(String[] args) throws Exception{
        Peer myClient = new Peer();
        myClient.registerTracker("please send ip address list, thanks");
        for (InetAddress key : myClient.peers.keySet())
        {
            System.out.println(key.getHostAddress()+":"+myClient.peers.get(key));

        }
    }


}