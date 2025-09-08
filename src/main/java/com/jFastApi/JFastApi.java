package com.jFastApi;

import com.jFastApi.app.AppContext;
import com.jFastApi.app.http.RouteScanner;
import com.jFastApi.app.http.interceptor.InterceptorScanner;
import com.jFastApi.app.util.BannerUtility;
import com.jFastApi.app.util.PropertiesUtil;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Main entry point for the JFastApi mini REST framework.
 * Initializes application context, scans routes, and starts the HTTP server.
 */
public final class JFastApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(JFastApi.class);
    private static final String PORT_NUMBER = "port.number"; // Key in app.properties

    public static void main(String[] args) {

        try {
            // Read port number from properties
            int port = PropertiesUtil.getPropertyInteger(PORT_NUMBER);

            // Create HTTP server bound to the port
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            // Use virtual threads (Java 24+) for handling requests
            server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

            // Initialize application context (sets base package for scanning)
            AppContext.initialize(JFastApi.class);

            // Scan base package for controller routes and register them
            RouteScanner.scanAndRegister(AppContext.getBasePackage());

            // Register interceptors
            InterceptorScanner.scanAndRegister(AppContext.getBasePackage());

            // Register dispatcher to handle incoming requests
            RouteScanner.registerDispatcher(server);

            // Print banner
            String banner = BannerUtility.getBANNER();
            System.out.println(banner);
            LOGGER.info("JFastAPi is listening on port : {}", port);

            // Add a shutdown hook for graceful server shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down server...");
                server.stop(0);
            }));

            // Start HTTP server
            server.start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
