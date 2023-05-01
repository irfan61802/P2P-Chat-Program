import java.net.*;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.io.*;

public class Tracker {

    static DatagramSocket serverSocket;
    public static HashMap<InetAddress, Integer> peers = new HashMap<InetAddress, Integer>();


    public static void sendToAll(InetAddress ipAddress, int port){
        
         //send list of clients in one string packet with the format "ip:port,ip:port,ip:port...etc"
         String[] peersArr= new String[peers.size()];
         int i=0;
         for (InetAddress key : peers.keySet())
         {
            peersArr[i]=key.getHostAddress()+":"+peers.get(key);
            i+=1;
         }
         String message = Stream.of(peersArr).collect(Collectors.joining(","));

         //send message client list
         try {
         byte[] buf;
         buf = message.getBytes();
         DatagramPacket packet;
         for (InetAddress key : peers.keySet())
         {
            packet = new DatagramPacket(buf, buf.length, key, peers.get(key));
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
            System.out.println("Waiting for Clients on Port 1234...");
            
            //create packet to receive data
            DatagramPacket receivePacket = new DatagramPacket(new byte[1024], 1024);
            serverSocket.receive(receivePacket);
            
            //get address and port from datagram and save to list
            InetAddress ipAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            String ip = ipAddress.toString();
            peers.put(ipAddress,port);        //saving client info to hashMap
            String received = new String(receivePacket.getData(), 0, receivePacket.getLength());
            
            //print when new client connected
            System.out.println("RECEIVED: " + ip + ":" + port + "\nmessage:" +received);

            
            if(received.equals("disconnect")){
                peers.remove(ipAddress);
            }
            sendToAll(ipAddress, port);
            

            //print out the current list of peers
            for (InetAddress key : peers.keySet())
            {
               System.out.println(key.getHostAddress()+":"+peers.get(key));
            }

        }
    }

}