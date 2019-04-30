/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.demo.nacos.configuration;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.spi.SpiOrder;
import com.alibaba.csp.sentinel.transport.HeartbeatSender;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.csp.sentinel.util.HostNameUtil;
import com.alibaba.csp.sentinel.util.PidUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.example.demo.nacos.enums.ConfigChangeType;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @author Eric Zhao
 * @author leyou
 */
@SpiOrder(SpiOrder.LOWEST_PRECEDENCE - 101)
public class HttpHeartbeatSender extends SentinelHttpCommon implements HeartbeatSender {

    private static final Logger logger = LoggerFactory.getLogger(HttpHeartbeatSender.class);

    public HttpHeartbeatSender() {
        init();
    }

    private void init() {
        logger.info("[HttpHeartbeatSender] Sending first heartbeat to {}:{}", consoleHost, consolePort);
        sendHeartbeatWithRetry(5);
        for (ConfigChangeType changeType : ConfigChangeType.values()) {
            // default retry three times,if always failure,will send four times
            SentinelConfigChangeSender.sendChangeRequestWithRetry(changeType, SentinelConfigConstant.CLIENT_INIT_OPERATOR);
        }
    }

    private void sendHeartbeatWithRetry(int retryCount) {
        RetryWrap retryWrap = new RetryWrap(retryCount) {
            @Override
            protected boolean doing() {
                return sendHeartbeat();
            }

            @Override
            protected void afterFailure() {
                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException e) {
                    logger.error("Thread fail to sleep and will start the next retry");
                }
            }
        };
        retryWrap.run();
    }

    @Override
    public boolean sendHeartbeat() {
        //Since HttpServer startup is asynchronous and sending useless ports to dashboard is unnecessary,
        // the sending condition add TransportConfig. getRuntimePort () < 0.
        if (StringUtil.isEmpty(consoleHost) || TransportConfig.getRuntimePort() < 0) {
            return false;
        }
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme("http").setHost(consoleHost).setPort(consolePort)
                    .setPath("/registryV2/machine")
                    .setParameter("v", Constants.SENTINEL_VERSION)
                    .setParameter("info", generateParam());

        HttpGet request;
        int statusCode;
        try {
            request = new HttpGet(uriBuilder.build());
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

    private String generateParam() {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, "localhost:8848");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("app",  AppNameUtil.getAppName());
        jsonObject.put("version", String.valueOf(System.currentTimeMillis()));
        jsonObject.put("hostname", HostNameUtil.getHostName());
        jsonObject.put("ip", TransportConfig.getHeartbeatClientIp());
        jsonObject.put("port", TransportConfig.getRuntimePort());
        jsonObject.put("pid", String.valueOf(PidUtil.getPid()));
        jsonObject.put("degradeRulesKey", RulesKeyUtils.getDegradeRulesKey());
        jsonObject.put("flowRulesKey", RulesKeyUtils.getFlowRulesKey());
        jsonObject.put("authorityRulesKey", RulesKeyUtils.getAuthorityRulesKey());
        jsonObject.put("systemRulesKey", RulesKeyUtils.getSystemRulesKey());
        jsonObject.put("paramFlowRulesKey", RulesKeyUtils.getParamFlowRulesKey());
        jsonObject.put("group", SentinelConfigConstant.NACOS_RULES_GROUP);
        jsonObject.put("timeoutMs", "3000");
        jsonObject.put("properties", JSON.toJSONString(properties));
        jsonObject.put("dataSourceType", SentinelConfigConstant.DATASOURCE_NACOS);
        return jsonObject.toJSONString();
    }

    @Override
    public long intervalMs() {
        return 5000;
    }

}
