import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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

            DatagramSocket socket = new DatagramSocket(port, InetAddress.getByName(ipAddress));
            System.out.println(ROLE + " DNS Server running on " + ipAddress + ":" + port);

            while (true) {
                byte[] buffer = new byte[512];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String query = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received query: " + query);

                // Check if the query ends with ".com" and forward to TLD server
                String tldServerConfig = DNSConfig.get("TLDServer");
                String response = query.endsWith(".com") ? tldServerConfig : "Not found";

                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();
                byte[] responseData = response.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, clientAddress, clientPort);
                socket.send(responsePacket);
                System.out.println("Sent response: " + response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}