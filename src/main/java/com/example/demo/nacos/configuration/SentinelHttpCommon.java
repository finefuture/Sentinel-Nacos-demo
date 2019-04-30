package com.example.demo.nacos.configuration;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.function.Tuple2;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel http 公共类
 *
 * @author longqiang
 */
public abstract class SentinelHttpCommon {

    private static final int TIMEOUT_MS = 3000;
    private static final RequestConfig REQUEST_CONFIG = RequestConfig.custom()
            .setConnectionRequestTimeout(TIMEOUT_MS)
            .setConnectTimeout(TIMEOUT_MS)
            .setSocketTimeout(TIMEOUT_MS)
            .build();
    private static final HttpClientContext CONTEXT = HttpClientContext.create();
    private static final CookieStore COOKIE_STORE = new BasicCookieStore();
    private static final CloseableHttpClient CLIENT = HttpClientBuilder.create()
                                    .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
                                    .setRedirectStrategy(new DefaultRedirectStrategy())
                                    .setDefaultRequestConfig(REQUEST_CONFIG)
                                    .setDefaultCookieStore(COOKIE_STORE)
                                    .build();

    protected static String consoleHost;
    protected static int consolePort;

    static {
        List<Tuple2<String, Integer>> dashboardList = parseDashboardList();
        if (dashboardList == null || dashboardList.isEmpty()) {
            RecordLog.info("[NettyHttpHeartbeatSender] No dashboard available");
        } else {
            consoleHost = dashboardList.get(0).r1;
            consolePort = dashboardList.get(0).r2;
            RecordLog.info("[NettyHttpHeartbeatSender] Dashboard address parsed: <" + consoleHost + ':' + consolePort + ">");
            sendLoginRequestWithRetry(3);
        }
    }

    private static void sendLoginRequestWithRetry(int retryCount) {
        RetryWrap retryWrap = new RetryWrap(retryCount) {
            @Override
            protected boolean doing() {
                return sendLoginRequest();
            }

            @Override
            protected void afterFailure() {
                //do nothing
            }
        };
        retryWrap.run();
    }

    private static boolean sendLoginRequest() {
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme("http").setHost(consoleHost).setPort(consolePort)
                    .setPath("/auth/login")
                    .setParameter("username", "sentinel")
                    .setParameter("password", "sentinel");
        HttpPost request;
        try {
            request = new HttpPost(uriBuilder.build());
            // Send heartbeat request.
            CloseableHttpResponse response = execute(request);
            response.close();
        } catch (URISyntaxException | IOException e) {
            RecordLog.warn("Error when sendLoginRequest, {}", e);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static List<Tuple2<String, Integer>> parseDashboardList() {
        List<Tuple2<String, Integer>> list = new ArrayList<>();
        try {
            String ipsStr = TransportConfig.getConsoleServer();
            if (StringUtil.isBlank(ipsStr)) {
                RecordLog.warn("[NettyHttpHeartbeatSender] Dashboard server address is not configured");
                return list;
            }

            for (String ipPortStr : ipsStr.split(",")) {
                if (ipPortStr.trim().length() == 0) {
                    continue;
                }
                ipPortStr = ipPortStr.trim();
                if (ipPortStr.startsWith("http://")) {
                    ipPortStr = ipPortStr.substring(7);
                }
                if (ipPortStr.startsWith(":")) {
                    continue;
                }
                String[] ipPort = ipPortStr.trim().split(":");
                int port = 80;
                if (ipPort.length > 1) {
                    port = Integer.parseInt(ipPort[1].trim());
                }
                list.add(Tuple2.of(ipPort[0].trim(), port));
            }
        } catch (Exception ex) {
            RecordLog.warn("[NettyHttpHeartbeatSender] Parse dashboard list failed, current address list: " + list, ex);
            ex.printStackTrace();
        }
        return list;
    }

    protected static CloseableHttpResponse execute(HttpRequestBase request) throws IOException {
        return CLIENT.execute(request, CONTEXT);
    }
}
