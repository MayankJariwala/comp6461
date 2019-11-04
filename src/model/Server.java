package model;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Server {

    private boolean isVerbose = false;
    private int port = 8080;
    public static Path path = Paths.get("D:\\lab 2\\httpfsserver\\src");

    public Server() {
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
                    path = Paths.get(data);
                    break;
            }
        }
    }
}
