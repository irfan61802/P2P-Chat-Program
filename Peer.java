import java.io.*;
import java.net.*;
import java.util.HashMap;

import java.awt.event.*;
class Peer{

    private DatagramSocket socket;
    private InetAddress address;
    private byte[] buf;
    public HashMap<InetAddress, Integer> peers = new HashMap<InetAddress, Integer>();
    private Gui gui;

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
        new Thread(new PeerListener(myClient)).start();
        myClient.gui = new Gui();

        // Add a listener for the Send button
        myClient.gui.addSendButtonListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String message = "MESSAGE:"+myClient.gui.getMessage();
                    byte[] buffer = message.getBytes();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, myClient.address, 1234);
                    myClient.socket.send(packet);
                    myClient.gui.clearMessage();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

    }

    //Listen for when tracker sends message
    static class PeerListener implements Runnable {
        private Peer peer;

        public PeerListener(Peer peer) {
            this.peer = peer;
        }

        public void run() {
            try {
                while(true){
                    DatagramPacket receivePacket = new DatagramPacket(new byte[1024], 1024);
                    peer.socket.receive(receivePacket);
                    String received = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    peer.gui.addMessage(received);
                    System.out.println(received);
                }
                    
            } catch (Exception e) {
                    System.out.println("Connection closed: " + e.getMessage());
            } finally {
                    
            }
        }
    }


}