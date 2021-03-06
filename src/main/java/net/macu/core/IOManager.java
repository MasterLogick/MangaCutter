package net.macu.core;

import net.macu.UI.ViewManager;
import net.macu.settings.Settings;
import net.macu.util.SelfUpdater;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class IOManager {
    private static CloseableHttpClient client = null;
    private static CloseableHttpAsyncClient asyncClient = null;
    public static final String RELEASE_REPOSITORY = "https://github.com/MangaCutter/MangaCutter/releases";
    public static final String LATEST_SUFFIX = "/latest";
    public static final String DOWNLOAD_SUFFIX = "/download";
    public static final String JAR_SUFFIX = "MangaCutter.jar";

    public static String sendRequest(String uri) throws IOException {
        StringBuilder sb = new StringBuilder();
        CloseableHttpResponse response = client.execute(new HttpGet(uri));
        BufferedReader bf = new BufferedReader(new InputStreamReader(sendRawRequest(uri).getContent()));
        String s;
        while ((s = bf.readLine()) != null) {
            sb.append(s).append('\n');
        }
        bf.close();
        response.close();
        return sb.toString();
    }

    public static Future<HttpResponse> sendRawAsyncRequest(HttpHost host, HttpRequest request,
                                                           FutureCallback<HttpResponse> callback) throws IOException {
        return asyncClient.execute(host, request, callback);
    }

    public static HttpEntity sendRawRequest(HttpUriRequest rawRequest) throws IOException {
        return client.execute(rawRequest).getEntity();
    }

    public static HttpEntity sendRawRequest(String uri) throws IOException {
        return sendRawRequest(new HttpGet(uri));
    }

    public static BufferedImage downloadImage(HttpUriRequest rawRequest) throws IOException {
        return ImageIO.read(sendRawRequest(rawRequest).getContent());
    }

    public static BufferedImage downloadImage(String uri) throws IOException {
        return ImageIO.read(sendRawRequest(uri).getContent());
    }

    public static void initClient() {
        if (client == null) {
            client = HttpClients.custom()
                    .setRedirectStrategy(LaxRedirectStrategy.INSTANCE)
                    .setUserAgent(Settings.IOManager_UserAgent.getValue())
                    .setConnectionTimeToLive(20, TimeUnit.SECONDS)
                    .build();
        }
        if (asyncClient == null) {
            asyncClient = HttpAsyncClients.custom()
                    .setRedirectStrategy(LaxRedirectStrategy.INSTANCE)
                    .setUserAgent(Settings.IOManager_UserAgent.getValue())
                    .build();
            asyncClient.start();
        }
    }

    public static boolean checkUpdates(boolean forced) {
        CloseableHttpClient cs = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom().setRedirectsEnabled(false).build()).build();
        try {
            String location = cs.execute(new HttpGet(RELEASE_REPOSITORY + LATEST_SUFFIX))
                    .getFirstHeader("location").getValue();
            String tag = location.substring(location.lastIndexOf("/") + 1);
            if (!tag.equals(Main.getVersion())) {
                String jarUrl = RELEASE_REPOSITORY + DOWNLOAD_SUFFIX + "/" + tag + "/" + JAR_SUFFIX;
                if ((!forced ? ViewManager.showConfirmDialog("core.IOManager.checkUpdates.new_ver", null, jarUrl, jarUrl) :
                        ViewManager.showConfirmDialogForced("core.IOManager.checkUpdates.new_ver", null, jarUrl, jarUrl))) {
                    SelfUpdater.selfUpdate(jarUrl);
                }
                return false;
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        try {
            cs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
