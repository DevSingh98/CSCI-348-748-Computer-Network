import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class AuthoritativeServer {
    private static String ROLE = "AuthoritativeServer";

    public static void main(String[] args) {
        try {
            String config = DNSConfig.get(ROLE);
            if (config == null) {
                throw new RuntimeException(ROLE + " configuration not found!");
            }

            String[] parts = config.split(":");
            String ipAddress = parts[0];
            int port = Integer.parseInt(parts[1]);

            try (DatagramSocket socket = new DatagramSocket(port)) {
                System.out.println(ROLE + " running on " + ipAddress + ":" + port);

                while (true) {
                    byte[] buffer = new byte[512];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    InetAddress clientAddress = packet.getAddress();
                    int clientPort = packet.getPort();

                    String query = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("Received query: " + query + " from IP " + clientAddress + ":" + clientPort);

                    String response = processQuery(query);


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

    private static String processQuery(String query) {
        // Sample database to resolve domain names
        String[][] authDatabase = {
                {"www.cs748.com", "93.184.216.34"},
                {"www.cs348.com", "93.184.216.35"}
        };

        for (String[] record : authDatabase) {
            if (record[0].equals(query)) {
                return "Resolved:" + record[1];
            }
        }
        return "Not found";
    }
}
