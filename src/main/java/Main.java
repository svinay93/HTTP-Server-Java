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



            Socket client = serverSocket.accept(); // Wait for connection from client.
            InputStream input = client.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line = reader.readLine();
            System.out.println(line);
            String[] httpData = line.split(" ", 0);
            if(httpData[1].contains("echo")) {
                String[] data = httpData[1].split("/");
                System.out.println(data[2]);
                String res = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: ";
                res += data[2].length();
                res += "\r\n\r\n";
                res += data[2];
                client.getOutputStream().write(res.getBytes());
            }
            else if(httpData[1].equals("/")){
                client.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
            }
            else{
                client.getOutputStream().write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
            }


            System.out.println("accepted new connection");

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
