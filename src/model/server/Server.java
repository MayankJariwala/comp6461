package model.server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Server {

    private boolean isVerbose = false;
    private int port = 8080;
    private boolean overwrite = false;
    private boolean dispositionMode = false;
    public static String pathStr = ".";
    public static Path path = Paths.get(pathStr).normalize().toAbsolutePath();
    private static Server server = null;

    private Server() {
    }

    public static Server getServerObject() {
        if (server == null)
            server = new Server();
        return server;
    }

    public boolean isVerbose() {
        return isVerbose;
    }

    public int getPort() {
        return port;
    }

    public Path getPath() {
        return path;
    }

    public boolean isDispositionMode() {
        return dispositionMode;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public void setServerConfiguration(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String data = args[i];
            switch (data) {
                case "-v":
                    this.isVerbose = true;
                    break;
                case "-p":
                    i = i + 1;
                    data = args[i];
                    this.port = Integer.parseInt(data);
                    break;
                case "-d":
                    i = i + 1;
                    data = args[i];
                    path = Paths.get(data).normalize().toAbsolutePath();
                    break;
                case "overwrite":
                    i = i + 1;
                    data = args[i];
                    overwrite = Boolean.parseBoolean(data);
                    break;
            }
        }
    }

    public void listFilesOfServerDir(Socket socket, HashMap<String, String> headers) {
        try (Stream<Path> walk = Files.walk(Server.path)) {
            List<String> result = walk
                    .filter(Files::isRegularFile)
                    .map(filePath -> filePath.getFileName().toString())
                    .filter(s -> s.endsWith(filterFileExtension(headers)))
                    .collect(Collectors.toList());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeBytes("HTTP/1.0 200 OK\r\n");
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                dataOutputStream.writeBytes(entry.getKey() + ":" + entry.getValue() + "\r\n");
            }
            dataOutputStream.writeBytes("Content-Length:" + result.size() + "\r\n");
            dataOutputStream.writeBytes("\r\n");
            for (String data : result) {
                dataOutputStream.writeBytes(data + "\r\n");
            }
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeFile(Socket socket, Path clientAccessingPath, String data, HashMap<String, String> headers) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            if (!Files.exists(clientAccessingPath)) {
                File file = new File(clientAccessingPath.toString());
                boolean DirectoryCreated = file.getParentFile().mkdirs();
                boolean fileCreated = file.createNewFile();
                shouldPrintRequest("Requested Dir Creating Status: " + DirectoryCreated);
                shouldPrintRequest("Requested File Creating Status: " + fileCreated);
            }
            synchronized (this) {
                FileWriter fileWriter = new FileWriter(clientAccessingPath.toString(), !overwrite);
                fileWriter.write(data);
                fileWriter.close();
            }
            dataOutputStream.writeBytes("HTTP/1.0 200 OK\r\n");
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                dataOutputStream.writeBytes(entry.getKey() + ":" + entry.getValue() + "\r\n");
            }
            dataOutputStream.writeBytes("\r\n");
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readFile(Socket socket, Path clientAccessingPath, HashMap<String, String> headers) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        try {
            int contentLength = 0;
            StringBuilder content = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(clientAccessingPath.toString()));
            String line;
            dataOutputStream.writeBytes("HTTP/1.0 200 OK\r\n");
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                dataOutputStream.writeBytes(entry.getKey() + ":" + entry.getValue() + "\r\n");
            }
            if (dispositionMode)
                // Disposition, to allow browser to download files
                dataOutputStream.writeBytes("Content-Disposition:attachment\r\n");
            while ((line = br.readLine()) != null) {
                contentLength += line.length();
                content.append(line).append("\n");
            }
            br.close();
            dataOutputStream.writeBytes("Content-Length:" + contentLength + "\r\n");
            dataOutputStream.writeBytes("\r\n");
            dataOutputStream.writeBytes(content.toString() + "\r\n");
            dataOutputStream.writeBytes("\r\n");
            dataOutputStream.flush();
        } catch (IOException e) {
            dataOutputStream.writeBytes("HTTP/1.0 404 NOT FOUND\r\n");
            dataOutputStream.writeBytes("\r\n");
            dataOutputStream.flush();
        }
    }

    private String filterFileExtension(HashMap<String, String> headers) {
        String extension = ".txt";
        if (headers.size() > 0 && headers.containsKey("Content-Type")) {
            String headerValue = headers.get("Content-Type");
            switch (headerValue) {
                case "application/json":
                    extension = ".json";
                    break;
                case "text/html":
                    extension = ".html";
                    break;
                default:
                    extension = ".txt";
                    break;
            }
        }
        return extension;
    }

    public void shouldPrintRequest(String data) {
        if (isVerbose)
            System.out.println(data);
    }
}
