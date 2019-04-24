package com.example.demo.nacos.configuration;

import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.csp.sentinel.util.HostNameUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.example.demo.nacos.configuration.SentinelConfigConstant.AUTHORITY_RULES_KEY;
import static com.example.demo.nacos.configuration.SentinelConfigConstant.DEGRADE_RULES_KEY;
import static com.example.demo.nacos.configuration.SentinelConfigConstant.FLOW_RULES_KEY;
import static com.example.demo.nacos.configuration.SentinelConfigConstant.PARAM_FLOW_RULES_KEY;
import static com.example.demo.nacos.configuration.SentinelConfigConstant.SYSTEM_RULES_KEY;

/**
 * <p>RulesKey config support.</p>
 * <p>
 * RulesKey supports configuration loading by several ways by order:<br>
 * 1. Env<br>
 * 2. System.properties
 * </p>
 *
 * @author longqiang
 */
public class RulesKeyUtils {

    public static final String APP_ID = AppNameUtil.getAppName() + "_" + HostNameUtil.getIp() + "_" + System.getProperty("server.port");

    public static final String DEFAULT_FLOW_RULES_KEY = APP_ID + "_" + FLOW_RULES_KEY;

    public static final String DEFAULT_DEGRADE_RULES_KEY = APP_ID + "_" + DEGRADE_RULES_KEY;

    public static final String DEFAULT_SYSTEM_RULES_KEY = APP_ID + "_" + SYSTEM_RULES_KEY;

    public static final String DEFAULT_PARAM_FLOW_RULES_KEY = APP_ID + "_" + PARAM_FLOW_RULES_KEY;

    public static final String DEFAULT_AUTHORITY_RULES_KEY = APP_ID + "_" + AUTHORITY_RULES_KEY;
    /**
     * Http heartbeat param flowRulesKey config.
     */
    public static final String CONFIG_FLOW_RULES_KEY = "sentinel.apollo.flowRules";
    /**
     * Http heartbeat param degradeRulesKey config.
     */
    public static final String CONFIG_DEGRADE_RULES_KEY = "sentinel.apollo.degradeRules";
    /**
     * Http heartbeat param systemRulesKey config.
     */
    public static final String CONFIG_SYSTEM_RULES_KEY = "sentinel.apollo.systemRules";
    /**
     * Http heartbeat param paramFlowRulesKey config.
     */
    public static final String CONFIG_PARAM_FLOW_RULES_KEY = "sentinel.apollo.paramFlowRules";
    /**
     * Http heartbeat param authorityRulesKey config.
     */
    public static final String CONFIG_AUTHORITY_RULES_KEY = "sentinel.apollo.authorityRules";


    private static final ConcurrentMap<String, String> CACHE_MAP = new ConcurrentHashMap<>();

    @NonNull
    private static String getConfig(String name) {
        // env
        String val = System.getenv(name);
        if (StringUtils.isNotEmpty(val)) {
            return val;
        }
        // properties
        val = System.getProperty(name);
        if (StringUtils.isNotEmpty(val)) {
            return val;
        }
        return "";
    }

    public static String getConfig(String name, String defaultVal) {
        if (CACHE_MAP.containsKey(name)) {
            return CACHE_MAP.get(name);
        }
        String val = getConfig(name);
        if (StringUtils.isBlank(val)) {
            val = defaultVal;
        }
        CACHE_MAP.put(name, val);
        return val;
    }

    public static String getFlowRulesKey() {
        return getConfig(CONFIG_FLOW_RULES_KEY, DEFAULT_FLOW_RULES_KEY);
    }

    public static String getSystemRulesKey() {
        return getConfig(CONFIG_SYSTEM_RULES_KEY, DEFAULT_SYSTEM_RULES_KEY);
    }

    public static String getParamFlowRulesKey() {
        return getConfig(CONFIG_PARAM_FLOW_RULES_KEY, DEFAULT_PARAM_FLOW_RULES_KEY);
    }

    public static String getAuthorityRulesKey() {
        return getConfig(CONFIG_AUTHORITY_RULES_KEY, DEFAULT_AUTHORITY_RULES_KEY);
    }

    public static String getDegradeRulesKey() {
        return getConfig(CONFIG_DEGRADE_RULES_KEY, DEFAULT_DEGRADE_RULES_KEY);
    }

}
