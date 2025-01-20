import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("CHECK_DISCORD", 1234);
            System.out.println("Connected to server.");

            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader serverResponse = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String message, serverMessage;
            while (true) {
                // Sending a message to the server
                System.out.println("Enter message to send to server:");
                message = userInput.readLine();
                if ("exit".equalsIgnoreCase(message)) {
                    break;
                }
                output.println(message);

                // Reading the response from the server
                serverMessage = serverResponse.readLine();
                System.out.println("Server: " + serverMessage);
            }
            socket.close();
            System.out.println("Connection closed.");

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
