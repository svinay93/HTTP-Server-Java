import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");

        // Uncomment this block to pass the first stage
        //
        try {

            ServerSocket serverSocket = new ServerSocket(4221);

            // Since the tester restarts your program quite often, setting SO_REUSEADDR
            // ensures that we don't run into 'Address already in use' errors
            serverSocket.setReuseAddress(true);
            while(true) {
                Socket client = serverSocket.accept(); // Wait for connection from client.
                byte[] in = client.getInputStream().readNBytes(70);
                String str = new String(in, StandardCharsets.UTF_8);
                String data = str.split("\r\n")[0].split(" ")[1];
                System.out.println(data);
                if (data.equals("/") && data.length() == 1) {
                    client.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                } else {
                    client.getOutputStream().write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
                }
                client.close();
                System.out.println("accepted new connection");
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
