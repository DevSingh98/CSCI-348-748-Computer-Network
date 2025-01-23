import java.io.IOException;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        String message = "www.cs348.org";
        String serverAddress = "3.87.174.211";
        int serverPort = 1234;
        int timeout = 10000;

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(timeout);

            byte[] buffer = message.getBytes();
            InetAddress serverInetAddress = InetAddress.getByName(serverAddress);
            DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length, serverInetAddress, serverPort);

            socket.send(requestPacket);

            byte[] responseBuffer = new byte[512];
            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);

            socket.receive(responsePacket);

            String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
            System.out.println("Response received: " + response);
        } catch (SocketTimeoutException e) {
            System.out.println("Socket timed out waiting for a response.");
        } catch (IOException e) {
            System.err.println("IO Exception: " + e.getMessage());
        }
    }
}