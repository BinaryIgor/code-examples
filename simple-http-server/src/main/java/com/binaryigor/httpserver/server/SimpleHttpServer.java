package com.binaryigor.httpserver.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class SimpleHttpServer implements HttpServer {

    private static final String HTTP_NEW_LINE_SEPARATOR = "\r\n";
    private static final String HTTP_HEAD_BODY_SEPARATOR = HTTP_NEW_LINE_SEPARATOR + HTTP_NEW_LINE_SEPARATOR;
    private static final int HTTP_HEAD_BODY_SEPARATOR_BYTES = HTTP_HEAD_BODY_SEPARATOR.getBytes(StandardCharsets.US_ASCII).length;
    private static final int DEFAULT_PACKET_SIZE = 50_000;
    private static final String CONTENT_LENGTH_HEADER = "content-length";
    private static final String CONNECTION_HEADER = "connection";
    private static final String CONNECTION_KEEP_ALIVE = "keep-alive";

    private final Executor requestsExecutor;
    private final int port;
    private final AtomicReference<ServerSocket> serverSocket = new AtomicReference<>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private boolean verboseMode = false;
    private int connectionTimeout = 300_000;
    private int bodyAsFileThreshold = 500_000;
    private Path tmpFilesPath = Path.of(System.getProperty("java.io.tmpdir"), "simple-http-server");

    public SimpleHttpServer(Executor requestsExecutor, int port) {
        this.requestsExecutor = requestsExecutor;
        this.port = port;
    }

    public SimpleHttpServer(int port) {
        this(Executors.newVirtualThreadPerTaskExecutor(), port);
    }

    public SimpleHttpServer configure(Consumer<Config> configurer) {
        var config = new Config();
        config.connectionTimeout = connectionTimeout;
        config.verboseMode = verboseMode;
        config.bodyAsFileThreshold = bodyAsFileThreshold;
        config.tmpFilesPath = tmpFilesPath;

        configurer.accept(config);

        connectionTimeout = config.connectionTimeout;
        verboseMode = config.verboseMode;
        bodyAsFileThreshold = config.bodyAsFileThreshold;
        tmpFilesPath = config.tmpFilesPath;

        return this;
    }

    public Path tmpFilesPath() {
        return tmpFilesPath;
    }

    @Override
    public void start(HttpRequestHandler handler) {
        if (isServerRunning()) {
            throw new RuntimeException("Server is running on port %d already!".formatted(port));
        }
        startServer(handler);
    }

    private boolean isServerRunning() {
        return serverSocket.get() != null && running.get();
    }

    private void startServer(HttpRequestHandler handler) {
        try {
            serverSocket.set(new ServerSocket(port));
            running.set(true);
        } catch (Exception e) {
            throw new RuntimeException("Fail to start a SimpleHttpServer on %d".formatted(port), e);
        }

        var server = serverSocket.get();

        new Thread(() -> {
            try {
                while (true) {
                    var connection = server.accept();
                    connection.setSoTimeout(connectionTimeout);
                    requestsExecutor.execute(() -> handleRequest(connection, handler));
                }
            } catch (Exception e) {
                if (isServerRunning()) {
                    stop();
                    throw new RuntimeException("Fail to to accept next connection...", e);
                }
                System.out.println("Closing server...");
            }
        }).start();
    }

    private void handleRequest(Socket connection, HttpRequestHandler requestHandler) {
        try {
            var requestOpt = readRequest(connection);
            if (requestOpt.isEmpty()) {
                closeConnection(connection);
                return;
            }

            var request = requestOpt.get();
            if (verboseMode) {
                printRequest(request);
            }

            respondToRequest(connection, request, requestHandler);

            if (shouldReuseConnection(request.headers())) {
                if (verboseMode) {
                    System.out.println("Keeping connection alive");
                }
                handleRequest(connection, requestHandler);
            } else {
                if (verboseMode) {
                    System.out.println("Closing connection");
                }
                closeConnection(connection);
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Socket timeout, closing");
            closeConnection(connection);
        } catch (Exception e) {
            System.out.println("Problem while handling connection");
            e.printStackTrace();
            closeConnection(connection);
        }
    }

    private Optional<HttpRequest> readRequest(Socket connection) throws Exception {
        var stream = connection.getInputStream();
        var rawRequestHead = readRawRequestHead(stream);

        if (rawRequestHead.length == 0) {
            return Optional.empty();
        }

        var requestHead = new String(rawRequestHead, StandardCharsets.US_ASCII);
        var lines = requestHead.split(HTTP_NEW_LINE_SEPARATOR);

        var line = lines[0];
        var methodUrl = line.split(" ");
        var method = methodUrl[0];
        var url = methodUrl[1];

        var headers = readHeaders(lines);

        var bodyLength = getExpectedBodyLength(headers);
        if (bodyLength <= 0) {
            return Optional.of(new HttpRequest(method, url, headers));
        }

        var bodyStartIndex = requestHead.indexOf(HTTP_HEAD_BODY_SEPARATOR);
        if (bodyStartIndex < 0) {
            return Optional.of(new HttpRequest(method, url, headers));
        }

        var readBody = Arrays.copyOfRange(rawRequestHead,
                bodyStartIndex + HTTP_HEAD_BODY_SEPARATOR_BYTES,
                rawRequestHead.length);

        if (bodyLength <= bodyAsFileThreshold) {
            var body = readBody(stream, readBody, bodyLength);
            return Optional.of(new HttpRequest(method, url, headers, body));
        }

        var bodyFile = readBodyToFile(stream, readBody, bodyLength);
        return Optional.of(new HttpRequest(method, url, headers, bodyFile));
    }

    private int getExpectedBodyLength(Map<String, List<String>> headers) {
        try {
            return Integer.parseInt(headers.getOrDefault(CONTENT_LENGTH_HEADER, List.of("0")).getFirst());
        } catch (Exception ignored) {
            return 0;
        }
    }

    private byte[] readRawRequestHead(InputStream stream) throws Exception {
        var toRead = stream.available();
        if (toRead == 0) {
            toRead = DEFAULT_PACKET_SIZE;
        }

        var buffer = new byte[toRead];
        var read = stream.read(buffer);
        if (read <= 0) {
            return new byte[0];
        }

        return read == toRead ? buffer : Arrays.copyOf(buffer, read);
    }

    private Map<String, List<String>> readHeaders(String[] lines) {
        var headers = new HashMap<String, List<String>>();

        for (int i = 1; i < lines.length; i++) {
            var line = lines[i];
            if (line.isEmpty()) {
                break;
            }

            var keyValue = line.split(":", 2);
            var key = keyValue[0].toLowerCase().strip();
            var value = keyValue[1].strip();

            headers.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }

        return headers;
    }

    private byte[] readBody(InputStream stream, byte[] readBody, int expectedBodyLength) throws Exception {
        if (readBody.length == expectedBodyLength) {
            return readBody;
        }

        var result = new ByteArrayOutputStream(expectedBodyLength);
        transferBody(stream, result, readBody, expectedBodyLength);
        return result.toByteArray();
    }

    private void transferBody(InputStream source,
                              OutputStream target,
                              byte[] readBody,
                              int expectedBodyLength) throws Exception {
        target.write(readBody);

        var readBytes = readBody.length;
        var buffer = new byte[DEFAULT_PACKET_SIZE];

        while (readBytes < expectedBodyLength) {
            var read = source.read(buffer);
            if (read > 0) {
                target.write(buffer, 0, read);
                readBytes += read;
            } else {
                break;
            }
        }
    }

    private File readBodyToFile(InputStream stream, byte[] readBody, int expectedBodyLength) throws Exception {
        var bodyFile = tmpFilesPath.resolve("request-body-" + UUID.randomUUID()).toFile();
        Files.createDirectories(tmpFilesPath);
        try (var os = new FileOutputStream(bodyFile)) {
            transferBody(stream, os, readBody, expectedBodyLength);
        }
        return bodyFile;
    }

    private void closeConnection(Socket connection) {
        try {
            System.out.println("Closing connection...");
            connection.close();
        } catch (Exception ignored) {

        }
    }

    private void respondToRequest(Socket connection, HttpRequest request,
                                  HttpRequestHandler requestHandler) throws Exception {
        var res = requestHandler.handle(request);

        var os = connection.getOutputStream();

        var resHead = new StringBuilder("HTTP/1.1 %d".formatted(res.responseCode()));

        res.headers().forEach((k, vs) ->
                vs.forEach(v ->
                        resHead.append(HTTP_NEW_LINE_SEPARATOR)
                                .append(k)
                                .append(": ")
                                .append(v)));

        var hasBody = res.body() != null && res.body().length > 0;

        if (!hasBody && res.headers().get(CONTENT_LENGTH_HEADER) == null) {
            // firefox requires this, chrome closes connection without it (for lacking body)
            resHead.append(HTTP_NEW_LINE_SEPARATOR)
                    .append(CONTENT_LENGTH_HEADER + ": 0");
        }

        resHead.append(HTTP_HEAD_BODY_SEPARATOR);

        os.write(resHead.toString().getBytes(StandardCharsets.US_ASCII));

        if (hasBody) {
            os.write(res.body());
        }
    }

    private boolean shouldReuseConnection(Map<String, List<String>> headers) {
        return headers.getOrDefault(CONNECTION_HEADER, List.of(CONNECTION_KEEP_ALIVE))
                .getFirst()
                .equals(CONNECTION_KEEP_ALIVE);
    }

    private void printRequest(HttpRequest req) {
        System.out.println("Method: " + req.method());
        System.out.println("Url: " + req.url());
        System.out.println("Headers:");
        req.headers().forEach((k, v) -> {
            System.out.printf("%s - %s%n", k, v);
        });
        System.out.println("Body:");
        if (req.hasBodyInFile()) {
            System.out.printf("Large body, stored in tmp file under %s path%n", req.bodyFile());
        } else if (req.hasBody()) {
            System.out.println(new String(req.body(), StandardCharsets.UTF_8));
        } else {
            System.out.println("Body is empty");
        }
    }

    @Override
    public void stop() {
        if (isServerRunning()) {
            try {
                serverSocket.get().close();
            } catch (Exception e) {
                throw new RuntimeException("Fail to close the server", e);
            } finally {
                serverSocket.set(null);
                running.set(false);
            }
        }
    }

    public static class Config {
        public boolean verboseMode;
        public int connectionTimeout;
        public int bodyAsFileThreshold;
        public Path tmpFilesPath;
    }
}
