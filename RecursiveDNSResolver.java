import java.io.IOException;
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
            String resolverIP = parts[0];
            int resolverPort = Integer.parseInt(parts[1]);

            try (DatagramSocket socket = new DatagramSocket(resolverPort)) {
                System.out.println(ROLE + " listening on " + resolverIP + ":" + resolverPort);

                while (true) {
                    byte[] buffer = new byte[512];
                    DatagramPacket queryPacket = new DatagramPacket(buffer, buffer.length);
                    socket.receive(queryPacket);

                    String domain = new String(queryPacket.getData(), 0, queryPacket.getLength());
                    System.out.println("Received query for domain: " + domain);

                    String rootConfig = DNSConfig.get("RootServer");
                    if (rootConfig == null) {
                        System.out.println("RootServer configuration not found!");
                        continue;
                    }

                    String[] rootParts = rootConfig.split(":");
                    String rootServerIP = rootParts[0];
                    int rootPort = Integer.parseInt(rootParts[1]);
                    socket.setSoTimeout(10000);
                    String resolvedIP = resolveDomain(domain, rootServerIP, rootPort,socket);
                    String response = resolvedIP != null ? "Resolved:" + resolvedIP : "Failed to resolve " + domain;

                    InetAddress clientAddress = queryPacket.getAddress();
                    int clientPort = queryPacket.getPort();
                    byte[] responseData = response.getBytes();
                    DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, clientAddress, clientPort);
                    socket.send(responsePacket);
                    socket.setSoTimeout(0);
                    System.out.println("Sent response: " + response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String resolveDomain(String domain, String serverIP, int port, DatagramSocket socket) throws Exception {
                System.out.println("Sending query to server: " + serverIP + ":" + port + " for domain: " + domain);

                byte[] queryData = domain.getBytes();
                InetAddress serverAddress = InetAddress.getByName(serverIP);
                DatagramPacket queryPacket = new DatagramPacket(queryData, queryData.length, serverAddress, port);
                socket.send(queryPacket);

                byte[] buffer = new byte[2048];
                DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(responsePacket);
                } catch (java.net.SocketTimeoutException e) {
                    System.out.println("Timeout waiting for response from server: " + serverIP);
                }

                String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
                System.out.println("Response from server (" + serverIP + "): " + response);

                if (response.startsWith("Resolved:")) {
                    return response.split(":")[1];
                } else if (response.contains(":")) {
                    serverIP = response.split(":")[0];
                    port = Integer.parseInt(response.split(":")[1]);
                    System.out.println("Moving to the next server: " + serverIP + ":" + port);
                } else {
                    System.out.println("Unexpected response format: " + response);
                }
        return null;
    }
}