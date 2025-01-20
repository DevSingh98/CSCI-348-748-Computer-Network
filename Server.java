import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) {
        final int port = 1234;
        try (DatagramSocket serverSocket = new DatagramSocket(port)){
            System.out.println("Server is waiting for connection...");

            byte[] recvData = new byte[512];

            while(true) {
                DatagramPacket recvPacket = new DatagramPacket(recvData, recvData.length);
                serverSocket.receive(recvPacket);

                String website = new String(recvPacket.getData(), 0, recvPacket.getLength());

                //Turns the Server off
                if(website.equals("exit")) break;
                
                website = website + " TEMP PLACEHOLDER";
                System.out.println(website) ;
                //CACHE implementation
                /*Should be IF statement to check if query is in cache or not
                    if not, forward packet to Root DNS server
                */
                //Send it back to client for now
                //Get the Client Info to send back later
                InetAddress clientAddr = recvPacket.getAddress();
                int clientPort = recvPacket.getPort();

                DatagramPacket sendPacket = new DatagramPacket(website.getBytes(),website.length(),clientAddr,clientPort);
                serverSocket.send(sendPacket);
                System.out.println("Msg Sent");
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
