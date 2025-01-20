import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(1234);
            System.out.println("Server is waiting for connection...");

            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected.");

            PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String clientMsg;

            while ((clientMsg = input.readLine()) != null) {
                System.out.println("Client: " + clientMsg);

                String response = "Rogerdoger";
                output.println(response);
            }

            clientSocket.close();
            serverSocket.close();
            System.out.println("Connection closed.");

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
