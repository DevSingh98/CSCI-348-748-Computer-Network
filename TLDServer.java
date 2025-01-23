import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class TLDServer {
    private static String ROLE = "TLD";

    public static void main(String[] args) {
        try {
            String config = DNSConfig.get("TLDServer");
            if (config == null) {
                throw new RuntimeException("TLDServer configuration not found!");
            }

            String[] parts = config.split(":");
            String ipAddress = parts[0];
            int port = Integer.parseInt(parts[1]);

            try (DatagramSocket socket = new DatagramSocket(port)) {
                System.out.println(ROLE + " DNS Server running on " + ipAddress + ":" + port);

                while (true) {
                    byte[] buffer = new byte[512];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    InetAddress clientAddress = packet.getAddress();
                    int clientPort = packet.getPort();

                    String query = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("Received query: " + query + " from IP " + clientAddress + ":" + clientPort);

                    String response = forwardToAuth(query, socket);

                    byte[] responseData = response.getBytes();
                    DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, clientAddress, clientPort);
                    socket.send(responsePacket);
                    System.out.println("Sent response: " + response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static String forwardToAuth(String query, DatagramSocket socket) throws UnknownHostException {
        String serverConfig;
        if (query.endsWith(".com")) {
            serverConfig = DNSConfig.get("AuthoritativeServer");
        } else {
            return "Domain not supported" + query;
        }
        String[] parts = serverConfig.split(":");
        InetAddress authServerAddress = InetAddress.getByName(parts[0]);
        int authServerPort = Integer.parseInt(parts[1]);

        try {
            byte[] queryData = query.getBytes();
            DatagramPacket queryPacket = new DatagramPacket(queryData, queryData.length, authServerAddress, authServerPort);
            socket.send(queryPacket);
            System.out.println("Forwarding to " + authServerAddress+":"+authServerPort);

            byte[] responseBuffer = new byte[512];
            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
            socket.receive(responsePacket);

            return new String(responsePacket.getData(), 0, responsePacket.getLength());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
