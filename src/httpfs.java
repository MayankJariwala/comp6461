import model.client.ClientRequest;
import model.server.Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class httpfs {

    public static void main(String[] args) {
        httpfs httpfs = new httpfs();
        Server server = Server.getServerObject();
        server.setServerConfiguration(args);
        httpfs.startListening(server);
    }

    private void startListening(Server server) {
        try {
            final ServerSocket serverSocket = new ServerSocket(server.getPort(), 100000, InetAddress.getByName("127.0.0.1"));
            System.out.println("----------- Server Configuration ------------------");
            System.out.println("Server Status : Up");
            System.out.println("Server Port : " + server.getPort());
            System.out.println("Server Data Dir : " + server.getPath());
            System.out.println("Server Verbosity : " + server.isVerbose());
            System.out.println("----------- Server Configuration ------------------");
            while (true) {
                Socket clientRequestSocket = serverSocket.accept();
                Thread thread = new Thread(new ClientRequest(clientRequestSocket));
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
