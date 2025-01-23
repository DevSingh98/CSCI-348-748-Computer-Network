import java.io.IOException;
import java.net.*;

public class DNSServer {
    private static String ROLE = "Root";

    public static void main(String[] args) {
        try {
            String config = DNSConfig.get("RootServer");
            if (config == null) {
                throw new RuntimeException("RootServer configuration not found!");
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

                    //Forward to TLD
                    String response = forwardToTLD(query, socket);

                    byte[] responseData = response.getBytes();
                    DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, clientAddress, clientPort);
                    socket.send(responsePacket);
                    System.out.println("Sent: " + response + " to " + clientAddress + ":" + clientPort );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String forwardToTLD(String query, DatagramSocket socket) throws UnknownHostException {
        String tldServerConfig;
        if (query.endsWith(".com")) {
            tldServerConfig = DNSConfig.get("TLDServer");
        } else {
            return "Domain not supported " + query;
        }
        String[] parts = tldServerConfig.split(":");
        InetAddress tldServerAddress = InetAddress.getByName(parts[0]);
        int tldServerPort = Integer.parseInt(parts[1]);

        try  {

            byte[] queryData = query.getBytes();
            DatagramPacket queryPacket = new DatagramPacket(queryData, queryData.length, tldServerAddress, tldServerPort);
            socket.send(queryPacket);
            System.out.println("Forwarding to " + tldServerAddress + ":" + tldServerPort);

            byte[] responseBuffer = new byte[512];
            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
            socket.receive(responsePacket);

            return new String(responsePacket.getData(), 0, responsePacket.getLength());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}