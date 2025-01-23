import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class RecursiveDNSResolver {
    private static String ROLE = "RecursiveResolver";

    public static void main(String[] args) {
        System.out.println("Started: " + RecursiveDNSResolver.class.getName());
        try {
            String config = DNSConfig.get(ROLE);
            if (config == null) {
                throw new RuntimeException(ROLE + " configuration not found!");
            }

            String[] parts = config.split(":");
            String rootServerIP = parts[0];
            int port = Integer.parseInt(parts[1]);
            String domain = "www.cs748.com";

            String resolvedIP = resolveDomain(domain, rootServerIP, port);
            if (resolvedIP != null) {
                System.out.println(domain + " resolved to " + resolvedIP);
            } else {
                System.out.println("Failed to resolve " + domain);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String resolveDomain(String domain, String serverIP, int port) throws Exception {
        while (true) {
            DatagramSocket socket = new DatagramSocket();
            byte[] queryData = domain.getBytes();
            InetAddress serverAddress = InetAddress.getByName(serverIP);
            DatagramPacket queryPacket = new DatagramPacket(queryData, queryData.length, serverAddress, port);
            socket.send(queryPacket);

            byte[] buffer = new byte[512];
            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(responsePacket);

            String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
            System.out.println("Response from server (" + serverIP + "): " + response);

            socket.close();

            // Ensure the response is a valid resolution or a valid next server to query
            if (response.startsWith("Resolved:")) {
                return response.split(":")[1]; // IP of the resolved domain
            } else if (response.contains(":")) {
                // The response is a server IP to query next
                serverIP = response.split(":")[0];
                System.out.println("Moving to the next server: " + serverIP);
            } else {
                System.out.println("Unexpected response format: " + response);
                break;
            }
        }
        return null;
    }

}