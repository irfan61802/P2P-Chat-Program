import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.text.SimpleDateFormat;

import java.awt.event.*;
class Peer{

    private DatagramSocket socket;
    private InetAddress address;
    private byte[] buf;
    public HashMap<InetSocketAddress, String> peers = new HashMap<InetSocketAddress, String>();
    public ArrayList<InetSocketAddress> users = new ArrayList<InetSocketAddress>();
    private Gui gui;
    private String username;

    public Peer() throws IOException{
        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
    }

    public void registerTracker(String msg) throws IOException {
        //send packet to tracker
        buf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 1234);
        socket.send(packet);

    }

    public void leaveChat() throws IOException {
        //send packet to tracker
        String msg="disconnect";
        buf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 1234);
        socket.send(packet);
    }

    //Sends message to all connected users directly
    public void sendMessage(String message){
        for (InetSocketAddress key : this.users)
        {
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, key.getAddress(), key.getPort());
            try {
                socket.send(packet);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    //Format date value to MM/dd hh:mm
    private static String dateFormat(String time){
        long timestamp = Long.parseLong(time);
        Date date = new Date(timestamp);
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd hh:mm a");
        String formattedDate = formatter.format(date);
        return formattedDate;
    }
    

    public static void main(String[] args) throws Exception {
        Peer myClient = new Peer();
        new Thread(new PeerListener(myClient)).start();
        myClient.gui = new Gui();
    
        // Add a listener for the Send button
        myClient.gui.addSendButtonListener(new ActionListener() {
            private boolean isFirstMessage = true;
    
            public void actionPerformed(ActionEvent e) {
                try {
                    //Send username to tracker if first message
                    if (isFirstMessage) {
                        myClient.username = myClient.gui.getMessage();  // Set the username from the first message
                        myClient.registerTracker(myClient.gui.getMessage());
                        isFirstMessage = false;
                        myClient.gui.clearMessage();
                        myClient.gui.addMessage("Username set as: " + myClient.username);
                    //Check for discconnect message
                    } else if (myClient.gui.getMessage().equals(".")) {
                        myClient.leaveChat();
                        System.exit(0);
                    //Handle messages from user
                    } else {
                        String message = "MESSAGE:" + System.currentTimeMillis() +
                            String.format("From %s: %s", myClient.username, myClient.gui.getMessage());
                        myClient.sendMessage(message);
                        myClient.gui.clearMessage();
                    }
                    
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

                    // Check for packet prefixes
                    // Handle member list updating
                    if (received.startsWith("CONNECT:")) {
                        String peersList = received.substring("CONNECT:".length());
                        System.out.println("Handling Connect");
                        //receive packet and return
                        String[] peerStrings = peersList.split(",");
                        peer.users.clear();
                        peer.peers.clear();
                        for(int i=0;i<peerStrings.length;i++){
                            InetSocketAddress ipPort = new InetSocketAddress(InetAddress.getByName(peerStrings[i].split(":")[0]),Integer.parseInt(peerStrings[i].split(":")[1]));
                            peer.peers.put(ipPort,peerStrings[i].split(":")[2]);
                            peer.users.add(ipPort);
                        }
                        peer.gui.setMembers(new ArrayList<>(peer.peers.values()));
                    // Handle messages
                    } else if (received.startsWith("MESSAGE:")) {
                        String message = received.substring("MESSAGE:".length());
                        String time = dateFormat(message.substring(0, 13));
                        peer.gui.addMessage(time +" - "+ message.substring(13));
                        System.out.println(received);
                    } else {
                        // Handle other cases or unrecognized messages
                        System.out.println("Received unrecognized message: " + received);
                    }
                }
            } catch (Exception e) {
                    e.printStackTrace();
            }
        }
    }
}