package model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

public class Server {

    private boolean isVerbose = false;
    private int port = 8080;
    private Path path = Path.of("httpfs.java");

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
                    this.path = Path.of(data);
                    break;
            }
        }
    }
}
