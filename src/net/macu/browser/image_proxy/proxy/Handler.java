package net.macu.browser.image_proxy.proxy;

import net.macu.util.UnblockableBufferedReader;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Handler extends Thread {
    private static SocketFactory socketFactory = null;
    private static SSLSocketFactory sslSocketFactory = null;
    private static int Counter = 0;
    private final Socket browserSocket;
    private final boolean secure;
    private Socket targetSocket;
    private UnblockableBufferedReader browserReader;
    private PrintWriter browserWriter;
    private OutputStream browserStream;
    private UnblockableBufferedReader targetReader;
    private PrintWriter targetWriter;
    private OutputStream targetStream;
    private String protocolVersion = "HTTP/1.1";
    private boolean keepAlive = true;
    private int keepAliveMax = -1;
    private int keepAliveRequestCount = 0;
    private int keepAliveTimeout = -1;
    private int keepAliveTime = 0;
    private String lastTargetHost = "";
    private int lastTargetPort = -1;

    public Handler(Socket s, boolean secure) {
        this.browserSocket = s;
        setDaemon(true);
        setName("Handler-" + (Counter));
        Counter++;
        this.secure = secure;
    }

    private static Socket createSocket(String host, int port) throws IOException {
        try {
            if (sslSocketFactory == null)
                sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(host, port);
            socket.startHandshake();
            return socket;
        } catch (Exception e) {
            if (socketFactory == null)
                socketFactory = SocketFactory.getDefault();
            return socketFactory.createSocket(host, port);
        }
    }

    private static byte[] readFixedSizeBody(int contentLength, UnblockableBufferedReader bodyStream) throws IOException {
        byte[] body = new byte[contentLength];
        for (int filled = 0; filled < contentLength; ) {
            filled += bodyStream.read(body, filled, contentLength - filled);
        }
        return body;
    }

    private static byte[] readChunkedBody(UnblockableBufferedReader bodyStream) throws IOException {
        String buffer;
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        while ((buffer = bodyStream.readLine(false)) != null && !buffer.isEmpty()) {
            int size = Integer.parseInt(buffer, 16);
            if (size == 0) {
                bodyStream.readLine(true);
                break;
            }
            byte[] chunk = new byte[size];
            for (int filled = 0; filled < size; ) {
                filled += bodyStream.read(chunk, filled, size - filled);
            }
            body.write(chunk);
            bodyStream.readLine(true);
        }
        return body.toByteArray();
    }

    private static void sendMessage(String firstLine, List<Header> headers, byte[] body, PrintWriter writer, OutputStream rawOutput) throws IOException {
        writer.print(firstLine + "\r\n");
        headers.forEach(header -> writer.print(header.toString() + "\r\n"));
        writer.print("\r\n");
        writer.flush();
        if (body.length != 0) {
            rawOutput.write(body);
            rawOutput.flush();
        }
    }

    @Override
    public void run() {
        try {
            System.out.println(getName() + " started");
            browserReader = new UnblockableBufferedReader(browserSocket.getInputStream());
            browserStream = browserSocket.getOutputStream();
            browserWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(browserStream)));
            do {
                if (keepAlive) {
                    if (keepAliveMax != -1 && keepAliveMax <= keepAliveRequestCount) {
                        break;
                    } else {
                        keepAliveRequestCount++;
                    }
                    while (keepAliveTimeout >= keepAliveTime) {
                        if (browserReader.available() == 0) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            keepAliveTime += 100;
                        } else {
                            break;
                        }
                    }
                    if (keepAliveTimeout != -1 && keepAliveTimeout <= keepAliveTime) {
                        break;
                    } else {
                        keepAliveTimeout = 0;
                    }
                }
                String requestLine = browserReader.readLine(false);
                String[] requestParts = requestLine.split(" ");
                if (requestParts.length != 3) {
                    sendErrorStatusMessage(400, "Bad Request");
                    break;
                }
                protocolVersion = requestParts[2];
                if (!protocolVersion.equals("HTTP/1.1") && !protocolVersion.equals("HTTP/1")) {
                    sendErrorStatusMessage(400, "Bad Request");
                    break;
                }
                String requestMethod = requestParts[0];
                String requestUrl = requestParts[1];

                //read request headers
                String targetHost = "";
                int targetPort = secure ? 443 : 80;
                int requestContentLength = 0;
                String requestEncoding = "identity";
                ArrayList<Header> requestHeaders = new ArrayList<>();
                String buffer;
                while ((buffer = browserReader.readLine(false)) != null && !buffer.isEmpty()) {
                    Header h = new Header(buffer);
                    if (h.headerName.equals("Host")) {
                        int colonIndex = h.headerData.indexOf(":");
                        if (colonIndex != -1) {
                            targetHost = h.headerData.substring(0, colonIndex);
                            targetPort = Integer.parseInt(h.headerData.substring(colonIndex + 1));
                        } else {
                            targetHost = h.headerData;
                        }
                    }
                    if (h.headerName.equals("Content-Length")) {
                        requestContentLength = Integer.parseInt(h.headerData);
                        continue;
                    }
                    if (h.headerName.equals("Connection")) {
                        keepAlive = h.headerData.equals("keep-alive");
                    }
                    if (h.headerName.equals("Accept-Encoding")) {
                        continue;
                    }
                    if (h.headerName.equals("Keep-Alive")) {
                        String[] params = h.headerData.split(",");
                        for (String p :
                                params) {
                            p = p.trim();
                            String[] par = p.split("=");
                            switch (par[0]) {
                                case "timeout":
                                    keepAliveTimeout = Integer.parseInt(par[1]) * 1000;
                                    break;
                                case "max":
                                    keepAliveMax = Integer.parseInt(par[1]);
                                    break;
                            }
                        }
                    }
                    if (h.headerName.equals("Transfer-Encoding")) {
                        requestEncoding = h.headerData;
                        h.headerData = "identity";
                    }
                    requestHeaders.add(h);
                }
                byte[] requestBody;

                //read request body
                if (requestEncoding.equals("chunked")) {
                    requestBody = readChunkedBody(browserReader);
                } else if (requestEncoding.equals("identity")) {
                    requestBody = readFixedSizeBody(requestContentLength, browserReader);
                } else {
                    throw new UnsupportedOperationException("Unsupported encoding type: " + requestEncoding);
                }
                requestHeaders.add(new Header("Accept-Encoding", "identity"));
                requestHeaders.add(new Header("Content-Length", String.valueOf(requestBody.length)));
                if (requestMethod.equals("CONNECT")) {
                    HTTPSPipe.pipe(browserReader, browserStream, browserSocket);
                    System.out.println(getName() + " piped and stopped");
                    return;
                }

                if (!lastTargetHost.equals(targetHost) || lastTargetPort != targetPort) {
                    if (targetSocket != null) {
                        targetWriter.close();
                        targetReader.close();
                        targetSocket.close();
                    }
                    targetSocket = createSocket(targetHost, targetPort);
                    targetStream = targetSocket.getOutputStream();
                    targetWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(targetStream)));
                    targetReader = new UnblockableBufferedReader(targetSocket.getInputStream());
                    String targetAddress = targetSocket.getRemoteSocketAddress().toString();
                    targetPort = Integer.parseInt(targetAddress.substring(targetAddress.lastIndexOf(":") + 1));
                    System.out.println("-----new connection from " + Thread.currentThread().getName() + " to " + targetSocket.getRemoteSocketAddress().toString() + "-----");
                } else {
                    System.out.println("-----new request from " + Thread.currentThread().getName() + " to target " + targetSocket.getRemoteSocketAddress().toString() + "-----");
                }
                sendMessage(requestMethod + " " + requestUrl + " " + protocolVersion, requestHeaders, requestBody, targetWriter, targetStream);

                //read target response
                String responseLine = targetReader.readLine(false);
                String responseMethod = responseLine.substring(0, responseLine.indexOf(" "));
                responseLine = responseLine.substring(responseLine.indexOf(" ") + 1);
                int responseCode = Integer.parseInt(responseLine.substring(0, responseLine.indexOf(" ")));
                String responseDescription = responseLine.substring(responseLine.indexOf(" ") + 1);
                ArrayList<Header> responseHeaders = new ArrayList<>();
                int responseContentLength = 0;
                String responseEncoding = "identity";
                while ((buffer = targetReader.readLine(false)) != null && !buffer.isEmpty()) {
                    Header h = new Header(buffer);
                    if (h.headerName.equals("Content-Length")) {
                        responseContentLength = Integer.parseInt(h.headerData);
                        continue;
                    }
                    if (h.headerName.equals("Connection")) {
                        keepAlive = h.headerData.equals("keep-alive");
                    }
                    if (h.headerName.equals("Transfer-Encoding")) {
                        responseEncoding = h.headerData;
                        h.headerData = "identity";
                    }
                    responseHeaders.add(h);
                }
                byte[] responseBody;
                if (responseEncoding.equals("chunked")) {
                    responseBody = readChunkedBody(targetReader);
                } else if (responseEncoding.equals("identity")) {
                    responseBody = readFixedSizeBody(responseContentLength, targetReader);
                } else {
                    sendErrorStatusMessage(400, "Bad Request");
                    break;
                }
                responseHeaders.add(new Header("Content-Length", String.valueOf(responseBody.length)));
                sendMessage(responseMethod + " " + responseCode + " " + responseDescription, responseHeaders, responseBody, browserWriter, browserSocket.getOutputStream());
                lastTargetPort = targetPort;
                lastTargetHost = targetHost;
            } while (keepAlive);
            browserWriter.close();
            browserReader.close();
            browserSocket.close();
            targetWriter.close();
            targetReader.close();
            targetSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(getName() + " stopped");
    }

    private void sendErrorStatusMessage(int code, String description) {
        browserWriter.print(protocolVersion);
        browserWriter.print(" ");
        browserWriter.print(code);
        browserWriter.print(" ");
        browserWriter.print(description);
        browserWriter.print("\r\n\r\n");
        browserWriter.flush();
    }
}