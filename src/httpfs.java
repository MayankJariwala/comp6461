import model.Server;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class httpfs {

    public static void main(String[] args) {
        httpfs httpfs = new httpfs();
        Server server = new Server();
        server.setServerConfiguration(args);
        httpfs.startListening(server);
    }

    private void startListening(Server server) {
        try {
            final ServerSocket serverSocket = new ServerSocket(server.getPort());
            while (true) {
                Socket clientRequestSocket = serverSocket.accept();
                InputStreamReader inputStreamReader = new InputStreamReader(clientRequestSocket.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line = bufferedReader.readLine();
                while (!line.equals(Constants.EXIT)) {
                    System.out.println(line);
                    line = bufferedReader.readLine();
                }
                sendResponseToClient(clientRequestSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendResponseToClient(Socket clientRequestSocket) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(
                new OutputStreamWriter(clientRequestSocket.getOutputStream(), StandardCharsets.UTF_8));
        bufferedWriter.write("HTTP/1.0 200 OK\r\n");
        bufferedWriter.write("Content-Type: text/plain\r\n");
        bufferedWriter.flush();
        clientRequestSocket.close();
    }
}
