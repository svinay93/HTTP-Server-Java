import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class HttpServer {

    private ServerSocket serverSocket;
    private ExecutorService executorService;

    public HttpServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.serverSocket.setReuseAddress(true);
        this.executorService = Executors.newFixedThreadPool(10);
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void run() throws IOException {

        while (true) {

            Socket client = serverSocket.accept();
            this.executorService.submit(() -> {
                try {
                    handleRequest(client);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public void handleRequest(Socket client) throws IOException {
        // Wait for connection from client.
        InputStream input = client.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        String line = reader.readLine();

        String[] httpData = line.split(" ", 0);
        String res = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: ";
        if (httpData[1].contains("echo")) {
            String[] data = httpData[1].split("/");
            System.out.println(data[2]);

            res += data[2].length();
            res += "\r\n\r\n";
            res += data[2];
            client.getOutputStream().write(res.getBytes());
        } else if (httpData[1].equals("/")) {
            client.getOutputStream().write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
        } else if (httpData[1].equals("/user-agent")) {
            reader.readLine();
            String[] data = reader.readLine().split(" ");
            res += data[1].length();
            res += "\r\n\r\n";
            res += data[1];

            client.getOutputStream().write(res.getBytes());
        } else {
            client.getOutputStream().write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
        }


        System.out.println("accepted new connection");
    }


}
