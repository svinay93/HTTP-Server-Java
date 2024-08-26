import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private String directory;

    public static final String SIMPLE_200 = "HTTP/1.1 200 OK\r\n\r\n";
    public static final String NOT_FOUND = "HTTP/1.1 404 Not Found\r\n\r\n";


    public HttpServer(int port, String path) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.serverSocket.setReuseAddress(true);
        this.executorService = Executors.newFixedThreadPool(10);
        this.directory = path;
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
        String requestPath = line.split(" ", 0)[1];
        String res =
                "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: ";

        if (requestPath.contains("echo")) {
            String[] data = requestPath.split("/");
            System.out.println(data[2]);
            res += data[2].length();
            res += "\r\n\r\n";
            res += data[2];
            client.getOutputStream().write(res.getBytes());
        } else if (requestPath.equals("/")) {
            client.getOutputStream().write(SIMPLE_200.getBytes());

        } else if (requestPath.equals("/user-agent")) {
            reader.readLine();
            String[] data = reader.readLine().split(" ");
            res += data[1].length();
            res += "\r\n\r\n";
            res += data[1];
            client.getOutputStream().write(res.getBytes());
        } else if (requestPath.startsWith("/files")) {
            String fileName = requestPath.split("/")[2];

            File f = new File(this.directory + "/" + fileName);
            if (f.exists() && !f.isDirectory()) {
                // do something
                System.out.println("File exists");
                String content = Files.readString(Path.of(this.directory + "/" + fileName), StandardCharsets.UTF_8);
                System.out.println(content);
                //HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nContent-Length: 14\r\n\r\nHello, World!
                String out =
                        "HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nContent-Length: ";
                out += content.length();
                out += "\r\n\r\n";
                out += content;
                client.getOutputStream().write(out.getBytes());

            } else {
                client.getOutputStream().write(
                        NOT_FOUND.getBytes());
            }


        } else {
            client.getOutputStream().write(
                    NOT_FOUND.getBytes());
        }
        client.close();
        System.out.println("accepted new connection");
    }
}