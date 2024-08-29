import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    private final ServerSocket serverSocket;
    private final ExecutorService executorService;
    private final String directory;
    public static final String SIMPLE_200 = "HTTP/1.1 200 OK\r\n\r\n";
    public static final String NOT_FOUND = "HTTP/1.1 404 Not Found\r\n\r\n";

    public static final String CREATED_201 = "HTTP/1.1 201 Created\r\n\r\n";


    public HttpServer(int port, String path) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.serverSocket.setReuseAddress(true);
        this.executorService = Executors.newFixedThreadPool(10);
        this.directory = path;
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
        String[] request = line.split(" ", 0);
        if (request[0].equals("GET")) {
            handleGetRequest(reader, request[1], client);
        }else if(request[0].equals("POST")){
            handlePostRequest(reader, request[1], client);
        }

        client.close();
        System.out.println("accepted new connection");
    }

    private void handlePostRequest(BufferedReader reader, String requestPath, Socket client) throws IOException {
        System.out.println("POST "+ requestPath);


        if(requestPath.startsWith("/files")){

            String fileName = requestPath.split("/")[2];
            reader.readLine();
            reader.readLine();
            reader.readLine();
            String size = reader.readLine().split(" ")[1];
            reader.readLine();
            reader.readLine();
            StringBuffer bodyBuffer = new StringBuffer();
            while (reader.ready()) {
                bodyBuffer.append((char)reader.read());
            }
            String body = bodyBuffer.toString();
            System.out.println("here");
            System.out.println(this.directory+ fileName);
            System.out.println(body);
            File file = new File(this.directory + fileName);
            if (file.createNewFile()) {
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(body);
                fileWriter.close();
            }

            client.getOutputStream().write(CREATED_201.getBytes());
        }
        else{
            client.getOutputStream().write(NOT_FOUND.getBytes());
        }


    }

    private void handleGetRequest(BufferedReader reader, String requestPath, Socket client) throws IOException {
        String res =
                "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: ";
        if (requestPath.equals("/")) {
            client.getOutputStream().write(SIMPLE_200.getBytes());
        } else if (requestPath.startsWith("/echo")) {
            String data = requestPath.split("/")[2];
            res += data.length()+"\r\n\r\n"+data;
            client.getOutputStream().write(res.getBytes());
        } else if (requestPath.equals("/user-agent")) {
            reader.readLine();
            String data = reader.readLine().split(" ")[1];
            res += data.length()+"\r\n\r\n"+data;
            client.getOutputStream().write(res.getBytes());
        } else if (requestPath.startsWith("/files")) {
            String fileName = requestPath.split("/")[2];
            File f = new File(this.directory + "/" + fileName);
            if (f.exists() && !f.isDirectory()) {

                String content = Files.readString(Path.of(this.directory + "/" + fileName), StandardCharsets.UTF_8);
                String out = "HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nContent-Length: ";
                out += content.length()+"\r\n\r\n"+content;
                client.getOutputStream().write(out.getBytes());

            } else {
                client.getOutputStream().write(NOT_FOUND.getBytes());
            }
        } else {
            client.getOutputStream().write(NOT_FOUND.getBytes());
        }
    }
}