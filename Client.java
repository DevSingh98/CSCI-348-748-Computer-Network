import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        final int port = 1234;
        try(DatagramSocket socket = new DatagramSocket(port)){

            //Set IP addr of the Local DNS server
            InetAddress localDNSAddress = InetAddress.getByName("LOCALDNS");
            //Check Discord for IP since this git is currently public

            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            String website = userInput.readLine();

            //Convert string into packet
            DatagramPacket sendMsg = new DatagramPacket(website.getBytes(), website.length(), localDNSAddress, port);
            socket.send(sendMsg);
            /*
            -----------------------------------------------------------------------------------
             */

            // Receives packet from LOCAL DNS server
            byte[] recvData = new byte[512];
            DatagramPacket recvPacket = new DatagramPacket(recvData,recvData.length);
            socket.receive(recvPacket);

            //Converts packet into readable string
            String response = new String(recvPacket.getData(),0,recvPacket.getLength());
            System.out.println(response);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
