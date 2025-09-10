package com.jFastApi;

import com.jFastApi.db.PrimaryDataSourceConfig;
import com.jFastApi.exception.ApplicationException;
import com.jFastApi.exception.ExceptionHandlerRegistry;
import com.jFastApi.http.RouteScanner;
import com.jFastApi.http.interceptor.InterceptorScanner;
import com.jFastApi.security.SecurityContext;
import com.jFastApi.util.BannerUtility;
import com.jFastApi.util.JwtHelper;
import com.jFastApi.util.PropertiesUtil;
import com.jFastApi.util.StringUtility;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Main entry point for the JFastApi mini REST framework.
 * Initializes application context, scans routes, and starts the HTTP server.
 */
public final class JFastApiApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(JFastApiApplication.class);

    public static void run(Class<?> baseClass) {

        try {
            // record start time in nanoseconds
            long startTime = System.nanoTime();

            // Read port number from properties
            int port = PropertiesUtil.getPropertyInteger(PropertiesUtil.PORT_NUMBER, 8085);

            // Create HTTP server bound to the port
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            // Use virtual threads (Java 24+) for handling requests
            server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

            // Initialize application context (sets base package for scanning)
            AppContext.initialize(baseClass, JFastApiApplication.class);

            initSystemEntity(server);

            // Print banner
            String banner = BannerUtility.getBANNER();
            System.out.println(banner);
            LOGGER.info("JFastAPi is listening on port : {}", port);

            // Add a shutdown hook for graceful server shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down server...");
                server.stop(0);
            }));

            // Measure end time
            long endTime = System.nanoTime();
            long elapsedMillis = (endTime - startTime) / 1_000_000;
            double elapsedSeconds = elapsedMillis / 1000.0;

            // Log startup success
            LOGGER.info("JFastApi started in {} sec", String.format("%.2f", elapsedSeconds));

            // Start HTTP server
            server.start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initSecurity() {

        try {

            if (!SecurityContext.isEnabled()) {
                LOGGER.info("JWT Security disabled.");
                return;
            }

            String secretKey = PropertiesUtil.getProperty(PropertiesUtil.SECURITY_SECRET_KEY);
            if (StringUtility.isEmpty(secretKey)) {
                throw new ApplicationException("JWT secret key must be provided when JWT security is enabled.");
            }

            // Register a bean of Jwt Helper
            JwtHelper jwtHelper = new JwtHelper(secretKey);
            BeanFactory.register(JwtHelper.class, jwtHelper);
        } catch (ApplicationException ex) {
            LOGGER.error(ex.getMessage());
            throw new ApplicationException(ex);
        } catch (Exception ex) {
            LOGGER.error("Failed to initialize security! error {}", ex.getMessage());
        }
    }

    private static void initSystemEntity(HttpServer server) {

        // Scan base package for controller routes and register them
        BeanFactory.scanAndRegister(AppContext.getBasePackage(), AppContext.getInternalBasePackage());

        initSecurity();

        PrimaryDataSourceConfig.init();

        // Scan base package for controller routes and register them
        RouteScanner.scanAndRegister(AppContext.getBasePackage(), AppContext.getInternalBasePackage());

        // Register interceptors
        InterceptorScanner.scanAndRegister(AppContext.getBasePackage(), AppContext.getInternalBasePackage());

        // Register Exception Handlers
        ExceptionHandlerRegistry.scanPackage(AppContext.getBasePackage(), AppContext.getInternalBasePackage());

        // Register dispatcher to handle incoming requests
        RouteScanner.registerDispatcher(server);
    }
}
