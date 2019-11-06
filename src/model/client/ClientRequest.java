package model.client;

import enumeration.Action;
import model.server.Server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;

public class ClientRequest extends ClientConfiguration implements Runnable {

    private String method;
    private String accessPath;
    private HashMap<String, String> headers = new HashMap<>();
    private String data = "";
    private Action action;
    private Server server;

    public ClientRequest(Socket socket) {
        super();
        this.socket = socket;
        server = Server.getServerObject();
    }

    private void execute() throws IOException {
        switch (this.action) {
            case LIST_FILES:
                server.listFilesOfServerDir(socket, headers);
                break;
            case WRITE_FILE:
                server.writeFile(socket, clientAccessingPath, data, headers);
                break;
            case READ_FILE:
                server.readFile(socket, clientAccessingPath, headers);
                break;
            default:
                break;
        }
    }

    private boolean isAuthorizedRequest() {
        String path = Server.pathStr + accessPath;
        clientAccessingPath = Server.path.resolve(path).normalize().toAbsolutePath();
        return !clientAccessingPath.startsWith(Server.path);
    }

    private void parseClientRequest() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line = bufferedReader.readLine();
        String[] requestInfo = line.split(" ");
        this.method = requestInfo[0].toLowerCase();
        this.accessPath = requestInfo[1];
        setClientOperation();
        while (line != null) {
            if (line.contains(":")) {
                String[] headerKeyAndValue = line.split(":", 2);
                headers.put(headerKeyAndValue[0].trim(), headerKeyAndValue[1].trim());
            }
            if (line.isEmpty()) {
                break;
            }
            server.shouldPrintRequest(line);
            line = bufferedReader.readLine();
        }
        StringBuilder stringBuilder = new StringBuilder();
        while (bufferedReader.ready()) {
            char data = (char) bufferedReader.read();
            stringBuilder.append(data);
        }
        if (method.equals("post")) {
            data = stringBuilder.toString();
            // Just to pretending like getting request directly from server
            server.shouldPrintRequest("\n");
            server.shouldPrintRequest(data);
        }
    }

    private void setClientOperation() {
        if (method.equals("get")) {
            if (accessPath.equals("/"))
                this.action = Action.LIST_FILES;
            else
                this.action = Action.READ_FILE;
        } else
            this.action = Action.WRITE_FILE;
    }

    private void sendUnauthorizedRequest() throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataOutputStream.writeBytes("HTTP/1.0 401 UNAUTHORIZED\r\n");
        dataOutputStream.writeBytes("Content-Type:application/json\r\n");
        dataOutputStream.writeBytes("\r\n");
        dataOutputStream.writeBytes("{\"status\":\"ACCESS DENIED\"}\r\n");
        dataOutputStream.writeBytes("\r\n");
        dataOutputStream.flush();
        dataOutputStream.close();
    }

    @Override
    public void run() {
        try {
            parseClientRequest();
            if (isAuthorizedRequest())
                sendUnauthorizedRequest();
            else
                execute();
            this.socket.close();
        } catch (IOException e) {
            System.out.println("Exception Catch : " + e.getLocalizedMessage());
        }
    }
}
