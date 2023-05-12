import java.net.*;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.io.*;

public class Tracker {

    static DatagramSocket serverSocket;
    // Hashmap Key is InetSocketAddress contains ip and port , value is username
    public static HashMap<InetSocketAddress, String> peers = new HashMap<InetSocketAddress, String>();


    public static void sendToAll(){
        
         //send list of clients in one string packet with the format "ip:port,ip:port,ip:port...etc"
         String[] peersArr= new String[peers.size()];
         int i=0;
         for (InetSocketAddress key : peers.keySet())
         {
            peersArr[i]=key.getAddress().getHostAddress()+":"+key.getPort()+":"+peers.get(key);
            i+=1;
         }
         String message = "CONNECT:"+Stream.of(peersArr).collect(Collectors.joining(","));

         //send message client list
         try {
         byte[] buf;
         buf = message.getBytes();
         DatagramPacket packet;
         for (InetSocketAddress key : peers.keySet())
         {
            packet = new DatagramPacket(buf, buf.length, key.getAddress(), key.getPort());
            serverSocket.send(packet);
         }
         } catch (IOException e) {
             System.err.println(e);
         }   
     
    }

    public static void sendMessage(String message){
        for (InetSocketAddress key : peers.keySet())
        {
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, key.getAddress(), key.getPort());
            try {
                serverSocket.send(packet);
            } catch (Exception e) {
                // TODO: handle exception
            }
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
            if(!peers.containsKey(ipPort)){
                peers.put(ipPort,received);        //saving client info to hashMap
                sendToAll();
            }

            if(received.equals("disconnect")){
                peers.remove(ipPort);
                sendToAll();
            }
            
            //print out the current list of peers
            for (InetSocketAddress key : peers.keySet())
            {
               System.out.println(key.getAddress().getHostAddress()+":"+peers.get(key));
            }

        }
    }

}