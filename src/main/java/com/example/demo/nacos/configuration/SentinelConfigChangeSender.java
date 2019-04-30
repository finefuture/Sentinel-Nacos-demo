package com.example.demo.nacos.configuration;

import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.example.demo.nacos.enums.ConfigChangeType;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Sentinel-Apollo 配置变化发送http请求至监听类
 *
 * @author longqiang
 */
public class SentinelConfigChangeSender extends SentinelHttpCommon {

    private static final Logger logger = LoggerFactory.getLogger(SentinelConfigChangeSender.class);
    private static int retryCount = 3;

    public static void setRetryCount(int retryCount) {
        SentinelConfigChangeSender.retryCount = retryCount;
    }

    public static int getRetryCount() {
        return retryCount;
    }

    public static void sendChangeRequestWithRetry(ConfigChangeType changeType, String operator) {
        RetryWrap retryWrap = new RetryWrap(retryCount) {
            @Override
            protected boolean doing() {
                return sendChangeRequest(changeType, operator);
            }

            @Override
            protected void afterFailure() {
                //do nothing
            }
        };
        retryWrap.run();
    }

    public static boolean sendChangeRequest(ConfigChangeType changeType, String operator) {
        if (StringUtil.isEmpty(consoleHost) || TransportConfig.getRuntimePort() < 0) {
            return false;
        }
        logger.info("[ChangeRequest] Sending change request to {}:{}, type:{}", consoleHost, consolePort, changeType);

        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme("http").setHost(consoleHost).setPort(consolePort)
                    .setPath(String.format("/configChange/%s", changeType.getType()))
                    .setParameter("app", AppNameUtil.getAppName())
                    .setParameter("ip", TransportConfig.getHeartbeatClientIp())
                    .setParameter("port", String.valueOf(TransportConfig.getRuntimePort()))
                    .setParameter("operator", operator);

        HttpPut request;
        int statusCode;
        try {
            request = new HttpPut(uriBuilder.build());
            CloseableHttpResponse response = execute(request);
            StatusLine statusLine = response.getStatusLine();
            statusCode = statusLine.getStatusCode();
            response.close();
        } catch (URISyntaxException | IOException e) {
            logger.error("Error when sendChangeRequest, {}", e);
            return false;
        }
        return statusCode == 200;
    }

}
