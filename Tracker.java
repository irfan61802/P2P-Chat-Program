import java.net.*;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.io.*;

public class Tracker {

    static DatagramSocket serverSocket;
    // Hashmap Key is InetSocketAddress contains ip and port , value is username
    public static HashMap<InetSocketAddress, String> peers = new HashMap<InetSocketAddress, String>();

    //Method to send connected user or disconnect user information to all other users
    public static void sendToAll(String method, String hostname, int port, String username){
         String message = method+hostname+":"+port+":"+username;
         //Send newly connected client information to each member
         try {
         byte[] buf;
         buf = message.getBytes();
         DatagramPacket packet;
         for (InetSocketAddress key : peers.keySet()){
            if((hostname.equals(key.getAddress().getHostAddress())) && port == key.getPort()){
                continue;
            }
            packet = new DatagramPacket(buf, buf.length, key.getAddress(), key.getPort());
            serverSocket.send(packet);
         }
         } catch (IOException e) {
             System.err.println(e);
         }   
    }
    
    public static void main(String[] args) throws Exception{
            
        //create a socket for udp on port 1234
        serverSocket = new DatagramSocket(1234);

        while(true){
            
            //create packet to receive data
            DatagramPacket receivePacket = new DatagramPacket(new byte[1024], 1024);
            serverSocket.receive(receivePacket);
            
            //get address and port from datagram and save to list
            InetAddress ipAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            InetSocketAddress ipPort = new InetSocketAddress(ipAddress, port);
            String received = new String(receivePacket.getData(), 0, receivePacket.getLength());

            //Handle new users
            if(!peers.containsKey(ipPort)){
                peers.put(ipPort,received);        //saving client info to hashMap
                sendToAll("CONNECT:",ipPort.getAddress().getHostAddress(),ipPort.getPort(),received);
                System.out.println(peers.size());
                //Send list of clients in one string packet with the format "ip:port:username,ip:port:username...etc"
                String[] peersArr= new String[peers.size()];
                int i=0;
                for (InetSocketAddress key : peers.keySet())
                {
                    peersArr[i]=key.getAddress().getHostAddress()+":"+key.getPort()+":"+peers.get(key);
                    i+=1;
                }
                String message = "INITIAL:"+Stream.of(peersArr).collect(Collectors.joining(","));
                //Send message client list to newly connected member
                try {
                    byte[] buf;
                    buf = message.getBytes();
                    DatagramPacket packet;
                    packet = new DatagramPacket(buf, buf.length, ipPort.getAddress(), ipPort.getPort());
                    serverSocket.send(packet);
                } catch (IOException e) {
                    System.err.println(e);
                }
            }

            //Handle disconnects
            if(received.startsWith("disconnect")){
                String username = received.substring("disconnect".length());
                peers.remove(ipPort);
                System.out.println(peers.size());
                System.out.println(peers.get(ipPort));
                sendToAll("DISCONNECT:",ipPort.getAddress().getHostAddress(),ipPort.getPort(),username);
            }

        }
    }

}