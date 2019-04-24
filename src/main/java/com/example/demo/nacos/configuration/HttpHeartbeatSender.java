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
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * @author Eric Zhao
 * @author leyou
 */
@SpiOrder(SpiOrder.LOWEST_PRECEDENCE - 101)
public class HttpHeartbeatSender extends SentinelHttpCommon implements HeartbeatSender {

    private static final Logger logger = LoggerFactory.getLogger(HttpHeartbeatSender.class);

    public HttpHeartbeatSender() {
        logger.info("[HttpHeartbeatSender] Sending first heartbeat to {}:{}", consoleHost, consolePort);
        try {
            sendHeartbeat();
            for (ConfigChangeType changeType : ConfigChangeType.values()) {
                // TODO need to retry the failed send?
                SentinelConfigChangeSender.sendChangeRequest(changeType, SentinelConfigConstant.CLIENT_INIT_OPERATOR);
            }
        } catch (Exception e) {
            logger.error("[HttpHeartbeatSender] Sending first heartbeat error:{}", e);
        }
    }

    @Override
    public boolean sendHeartbeat() throws Exception {
        if (StringUtil.isEmpty(consoleHost)) {
            return false;
        }
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme("http").setHost(consoleHost).setPort(consolePort)
                    .setPath("/registryV2/machine")
                    .setParameter("v", Constants.SENTINEL_VERSION)
                    .setParameter("info", generateParam());

        HttpGet request = new HttpGet(uriBuilder.build());
        // Send heartbeat request.
        CloseableHttpResponse response = execute(request);
        response.close();
        return true;
    }

    private String generateParam() {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, "localhost:8848");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("app",  AppNameUtil.getAppName());
        jsonObject.put("version", String.valueOf(System.currentTimeMillis()));
        jsonObject.put("hostname", HostNameUtil.getHostName());
        jsonObject.put("ip", TransportConfig.getHeartbeatClientIp());
        jsonObject.put("port", TransportConfig.getPort());
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
