import enumeration.Action;
import model.ClientRequest;
import model.Server;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class httpfs {

    public static void main(String[] args) {
        httpfs httpfs = new httpfs();
        Server server = new Server();
        server.setServerConfiguration(args);
        httpfs.startListening(server);
    }

    private void startListening(Server server) {
        try {
            final ServerSocket serverSocket = new ServerSocket(server.getPort(), 100000, InetAddress.getByName("127.0.0.1"));
            while (true) {
                Socket clientRequestSocket = serverSocket.accept();
                DataOutputStream dataOutputStream = new DataOutputStream(clientRequestSocket.getOutputStream());
                InputStreamReader inputStreamReader = new InputStreamReader(clientRequestSocket.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line = bufferedReader.readLine();
                String[] requestInfo = line.split(" ");
                String method = requestInfo[0].toLowerCase();
                String accessPath = requestInfo[1];
                ClientRequest clientRequest = new ClientRequest();
                clientRequest.setMethod(method);
                clientRequest.setAccessPath(accessPath);
                setAction(clientRequest, accessPath);
                clientRequest.setDataOutputStream(dataOutputStream);
                while (line != null) {
                    System.out.println(line);
                    if (line.isEmpty()) {
                        break;
                    }
                    line = bufferedReader.readLine();
                }
                StringBuilder stringBuilder = new StringBuilder();
                while (bufferedReader.ready()) {
                    char data = (char) bufferedReader.read();
                    stringBuilder.append(data);
                }
                if (clientRequest.getMethod().equals("post")) {
                    clientRequest.setData(stringBuilder.toString());
                    System.out.println(stringBuilder.toString());
                }
                clientRequest.execute();
                clientRequestSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setAction(ClientRequest clientRequest, String request) {
        if (clientRequest.getMethod().equals("get")) {
            if (request.equals("/"))
                clientRequest.setAction(Action.LIST_FILES);
            else
                clientRequest.setAction(Action.READ_FILE);
        } else
            clientRequest.setAction(Action.WRITE_FILE);
    }
}
