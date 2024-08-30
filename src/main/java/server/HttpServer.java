package server;



import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPOutputStream;


public class HttpServer {
    private final ServerSocket serverSocket;
    private final ExecutorService executorService;
    private final String directory;
    private static final String SIMPLE_200 = "HTTP/1.1 200 OK\r\n\r\n";
    private static final String NOT_FOUND_404 = "HTTP/1.1 404 Not Found\r\n\r\n";
    private static final String CREATED_201 = "HTTP/1.1 201 Created\r\n\r\n";
    private static final String PLAIN_TEXT_200 = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: ";
    private static final String OCTET_STREAM_200 = "HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nContent-Length: ";
    private static final String GZIP_BASE = "HTTP/1.1 200 OK\r\nContent-Encoding: gzip\r\nContent-Type: text/plain\r\nContent-Length: ";






    public HttpServer(int port, String path) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.serverSocket.setReuseAddress(true);
        this.executorService = Executors.newFixedThreadPool(10);
        this.directory = path;
    }


    public void run() throws IOException {
        while (true) {
            Socket client = serverSocket.accept();
            System.out.println("accepted new connection");
            this.executorService.submit(() -> {
                try {
                    handleRequest(client);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void handleRequest(Socket client) throws IOException {
        // Wait for connection from client.
        InputStream input = client.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line = reader.readLine();
        String[] request = line.split(" ", 0);
        Map<String, String> requestHeaders = new HashMap<String, String>();
        String header = null;
        while ((header = reader.readLine()) != null &&
                !header.isEmpty()) {
            String[] keyVal = header.split(":", 2);
            if (keyVal.length == 2) {
                requestHeaders.put(keyVal[0], keyVal[1].trim());
            }
        }
        if (request[0].equals("GET")) {
            handleGetRequest(reader, request[1], client, requestHeaders);
        } else if (request[0].equals("POST")) {
            handlePostRequest(reader, request[1], client, requestHeaders);
        }
        client.close();
    }

    private void handlePostRequest(BufferedReader reader, String requestPath, Socket client, Map<String, String> requestHeaders) throws IOException {

        if (requestPath.startsWith("/files")) {
            String fileName = requestPath.split("/")[2];
            StringBuffer bodyBuffer = new StringBuffer();
            while (reader.ready()) {
                bodyBuffer.append((char) reader.read());
            }
            String body = bodyBuffer.toString();
            File file = new File(this.directory + fileName);
            if (file.createNewFile()) {
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(body);
                fileWriter.close();
            }
            client.getOutputStream().write(CREATED_201.getBytes());
        }
        else {
            client.getOutputStream().write(NOT_FOUND_404.getBytes());
        }
    }

    private void handleGetRequest(BufferedReader reader, String requestPath, Socket client, Map<String, String> requestHeaders) throws IOException {

        if (requestPath.equals("/")) {
            client.getOutputStream().write(SIMPLE_200.getBytes());
        }
        else if (requestPath.startsWith("/echo")) {
            String data = requestPath.split("/")[2];
            if(requestHeaders.containsKey("Accept-Encoding") && requestHeaders.get("Accept-Encoding").contains("gzip")){
                byte[] compressedData = compress(data);
                byte[] base = (GZIP_BASE + compressedData.length + "\r\n\r\n").getBytes();
                client.getOutputStream().write(base);
                client.getOutputStream().write(compressedData);
                
            }
            else {
                sendResponse(PLAIN_TEXT_200, data, client);
            }
        }
        else if (requestPath.equals("/user-agent")) {
            String data = requestHeaders.get("User-Agent");
            sendResponse(PLAIN_TEXT_200, data, client);
        }
        else if (requestPath.startsWith("/files")) {

            String fileName = requestPath.split("/")[2];
            File f = new File(this.directory + "/" + fileName);
            if (f.exists() && !f.isDirectory()) {
                String content = Files.readString(Path.of(this.directory + "/" + fileName), StandardCharsets.UTF_8);
                sendResponse(OCTET_STREAM_200, content, client);
            } else {
                client.getOutputStream().write(NOT_FOUND_404.getBytes());
            }
        }
        else {
            client.getOutputStream().write(NOT_FOUND_404.getBytes());
        }
    }

    private void sendResponse(final String base, String data, Socket client) throws IOException {
        String res = base + data.length() + "\r\n\r\n" + data;
        client.getOutputStream().write(res.getBytes());
    }

    private byte[] compress(final String str) throws IOException {
        if ((str == null) || (str.length() == 0)) {
            return null;
        }
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(obj);
        gzip.write(str.getBytes("UTF-8"));
        gzip.flush();
        gzip.close();
        return obj.toByteArray();
    }


}