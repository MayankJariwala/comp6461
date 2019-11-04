package model;

import enumeration.Action;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientRequest {

    private String method;
    private String accessPath;
    private String data = "";
    private Action action;
    private DataOutputStream dataOutputStream;

    public ClientRequest() {

    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getAccessPath() {
        return accessPath;
    }

    public void setAccessPath(String accessPath) {
        this.accessPath = accessPath;
    }

    public void setDataOutputStream(DataOutputStream dataOutputStream) {
        this.dataOutputStream = dataOutputStream;
    }

    public String getMethod() {
        return method;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public void execute() {
        switch (this.action) {
            case LIST_FILES:
                System.out.println("List files");
                listFilesOfServerDir();
                break;
            case WRITE_FILE:
                System.out.println("Write File");
                writeFile();
                break;
            case READ_FILE:
                try {
                    readFile();
                } catch (IOException e) {
                    System.out.println(e.getLocalizedMessage());
                }
                break;
            default:
                break;
        }
    }

    private void listFilesOfServerDir() {
        try (Stream<Path> walk = Files.walk(Server.path)) {
            List<String> result = walk
                    .map(filePath -> filePath.getFileName().toString())
                    .filter(s -> s.endsWith("txt"))
                    .collect(Collectors.toList());
            this.dataOutputStream.writeBytes("HTTP/1.0 200 OK\r\n");
            this.dataOutputStream.writeBytes("Content-Type:application/json\r\n");
            this.dataOutputStream.writeBytes("\r\n");
            for (String data : result) {
                System.out.println(data);
                this.dataOutputStream.writeBytes(data + "\r\n");
            }
            this.dataOutputStream.writeBytes("\r\n");
            this.dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeFile() {
        String fileName = accessPath.substring(1) + ".txt";
        Path path = Server.path.resolve(fileName);
        try {
            FileWriter fileWriter = new FileWriter(path.toString());
            fileWriter.write(data);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readFile() throws IOException {
        String fileName = accessPath.substring(1) + ".txt";
        Path path = Server.path.resolve(fileName);
        try {
            BufferedReader br = new BufferedReader(new FileReader(path.toString()));
            String line;
            this.dataOutputStream.writeBytes("HTTP/1.0 200 OK\r\n");
            this.dataOutputStream.writeBytes("Content-Type:application/json\r\n");
            this.dataOutputStream.writeBytes("\r\n");
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                this.dataOutputStream.writeBytes(line + "\r\n");
            }
            this.dataOutputStream.writeBytes("\r\n");
            this.dataOutputStream.flush();
            br.close();
        } catch (IOException e) {
            this.dataOutputStream.writeBytes("HTTP/1.0 404 NOT FOUND\r\n");
            this.dataOutputStream.writeBytes("\r\n");
            this.dataOutputStream.flush();
        }
    }
}
