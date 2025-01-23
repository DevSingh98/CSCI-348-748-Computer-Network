import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;


public class RecursiveDNSResolver {
    private static String ROLE = "RecursiveResolver";

    public static void main(String[] args) {
        ArrayList<String[]> cache = new ArrayList<String[]>();
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

                    InetAddress clientAddress = queryPacket.getAddress();
                    int clientPort = queryPacket.getPort();

                    String domain = new String(queryPacket.getData(), 0, queryPacket.getLength());
                    System.out.println("Received query for domain: " + domain);
                    //Check cache for hits,if hit check TTL
                    boolean forward = true;
                    Instant instant = Instant.now();
                    if (!cache.isEmpty()) {
                        for (String[] record : cache) {
                            if (record[0].equals(domain) && (instant.getEpochSecond() - Long.parseLong(record[3])) < 60) {
                                //Sent hit instead of forwarding
                                byte[] cacheResponse = ("Cache Hit:"+record[1]).getBytes();
                                DatagramPacket responsePacket = new DatagramPacket(cacheResponse, cacheResponse.length, clientAddress, clientPort);
                                socket.send(responsePacket);
                                forward = false;
                            }
                        }
                    }
                    if(forward){
                    String rootConfig = DNSConfig.get("RootServer");
                    if (rootConfig == null) {
                        System.out.println("RootServer configuration not found!");
                        continue;
                    }

                    String[] rootParts = rootConfig.split(":");
                    String rootServerIP = rootParts[0];
                    int rootPort = Integer.parseInt(rootParts[1]);
                    socket.setSoTimeout(10000);
                    String resolvedIP = resolveDomain(domain, rootServerIP, rootPort, socket, cache);
                    String response; //= resolvedIP != null ? "Resolved:" + resolvedIP : "Failed to resolve " + domain;
                    if (resolvedIP != null) {
                        String[] part = (resolvedIP.split(","));
                        String[] newpart = Arrays.copyOf(part, 4);
                        newpart[3] = String.valueOf(instant.getEpochSecond());
                        cache.add(newpart);
                        response = "Resolved:"+ part[1];
                    }else {
                        response = "Failed to resolve " + domain;
                    }


                    byte[] responseData = response.getBytes();
                    DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, clientAddress, clientPort);
                    socket.send(responsePacket);
                    socket.setSoTimeout(0);
                    System.out.println("Sent response: " + response);
                    System.out.println();
                }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private static String resolveDomain(String domain, String serverIP, int port, DatagramSocket socket, ArrayList<String[]> cache) throws Exception {
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