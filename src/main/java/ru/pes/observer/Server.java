package ru.pes.observer;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Admin on 08.06.2016.
 */
public class Server implements Runnable {
    private final int PORT;
    private static final Logger logger = Logger.getLogger(Server.class);
    private ExecutorService service = Executors.newFixedThreadPool(100);

    public Server(int port) {
        logger.info("Server started");
        PORT = port;
    }

    @Override
    public void run() {
        ServerSocket server = null;

        try {
            server = new ServerSocket(PORT);
        } catch (IOException e) {
            logger.error("Порт " + PORT + " уже используется! ", e);
            System.exit(-1);
        }

        while (true) {
            Worker w;
            try {
                w = new Worker(server.accept());
                service.submit(w);
            } catch (IOException e) {
                logger.error("Неудалось создать новый поток ", e);
                System.exit(-1);
            }
        }
    }
}
