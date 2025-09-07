package com.jFastApi;

import com.jFastApi.app.AppContext;
import com.jFastApi.app.RouteScanner;
import com.jFastApi.app.util.BannerUtility;
import com.jFastApi.app.util.PropertiesUtil;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public final class JFastApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(JFastApi.class);
    private static final String PORT_NUMBER = "port.number";

    public static void main(String[] args) {

        try {
            int port = PropertiesUtil.getPropertyInteger(PORT_NUMBER);
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

            AppContext.initialize(JFastApi.class);
            RouteScanner.scanAndRegister(server, AppContext.getBasePackage());

            // Start
            String banner = BannerUtility.getBANNER();
            System.out.println(banner);
            LOGGER.info("JFastAPi is listening on port : {}", port);

            // Add a shutdown hook for graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down server...");
                server.stop(0);
            }));

            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
